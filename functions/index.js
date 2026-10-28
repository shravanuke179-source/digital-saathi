import { initializeApp } from 'firebase-admin/app';
import { getFirestore } from 'firebase-admin/firestore';
import { onCall, HttpsError } from 'firebase-functions/v2/https';
import { validateDeliveryUpdate, assertNextStatus, produceStatus } from './workflow.js';

initializeApp();

// Callable SDK verifies Firebase tokens; never trust a UID or admin flag in request.data.
// The browser/Android client has no service-account key. Deployment uses managed identity.
export const updateDeliveryStatus = onCall({ region: 'asia-south1', enforceAppCheck: true }, async request => {
  if (!request.auth) throw new HttpsError('unauthenticated', 'Sign in first.');
  if (request.auth.token.admin !== true) throw new HttpsError('permission-denied', 'An authorized operator is required.');
  let input;
  try { input = validateDeliveryUpdate(request.data); }
  catch (error) { throw new HttpsError('invalid-argument', error.message); }
  const database = getFirestore();
  const deliveryRef = database.doc(`users/${input.uid}/deliveries/${input.deliveryId}`);
  await database.runTransaction(async transaction => {
    const snapshot = await transaction.get(deliveryRef);
    if (!snapshot.exists) throw new HttpsError('not-found', 'Delivery not found.');
    const delivery = snapshot.data();
    try { assertNextStatus(delivery.status, input.status); }
    catch (error) { throw new HttpsError('failed-precondition', error.message); }
    const listingRef = database.doc(`users/${input.uid}/produce/${delivery.produceListingId}`);
    const listing = await transaction.get(listingRef);
    if (!listing.exists || listing.data().deliveryRequest?.id !== input.deliveryId) {
      throw new HttpsError('failed-precondition', 'Delivery does not match the produce listing.');
    }
    const timestamp = new Date().toISOString();
    const updated = { ...delivery, status: input.status, statusUpdatedAt: timestamp,
      trackingUpdates: [...delivery.trackingUpdates, { first: input.status, second: timestamp }] };
    transaction.set(deliveryRef, updated);
    transaction.update(listingRef, { deliveryRequest: updated, status: produceStatus[input.status] });
    transaction.set(database.collection('auditEvents').doc(), {
      actorUid: request.auth.uid, action: 'delivery.status', uid: input.uid, deliveryId: input.deliveryId,
      previous: delivery.status, next: input.status, at: timestamp
    });
  });
  return { success: true };
});
