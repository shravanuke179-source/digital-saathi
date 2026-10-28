import { readFile } from 'node:fs/promises';
import { before, after, beforeEach, test } from 'node:test';
import assert from 'node:assert/strict';
import { initializeTestEnvironment, assertFails, assertSucceeds } from '@firebase/rules-unit-testing';
import { doc, getDoc, setDoc, updateDoc, deleteDoc, writeBatch } from 'firebase/firestore';
import { initializeApp, deleteApp } from 'firebase/app';
import { getAuth, connectAuthEmulator, createUserWithEmailAndPassword, signInWithEmailAndPassword, signOut, sendPasswordResetEmail } from 'firebase/auth';

let environment;
before(async () => {
  environment = await initializeTestEnvironment({ projectId: 'demo-digital-saathi', firestore: {
    rules: await readFile(new URL('../firestore.rules', import.meta.url), 'utf8')
  } });
});
beforeEach(async () => environment.clearFirestore());
after(async () => environment?.cleanup());
const db = uid => uid ? environment.authenticatedContext(uid).firestore() : environment.unauthenticatedContext().firestore();
const profile = { name: 'Test Farmer', village: 'Test Village', district: 'Test District', phone: '9876543210', state: 'Test State' };
const produce = { id: 'p1', cropName: 'Wheat', category: 'Grains', quantity: 10, unit: 'Qtl', expectedPricePerUnit: 2500,
  qualityGrade: 'A', harvestDate: '2026-10-13', pickupLocation: 'Farm', preferredBuyerOrMandi: '', productPhoto: '',
  additionalNotes: '', listedDate: '2026-10-13', status: 'ACTIVE_LISTING' };

test('owner can create, read and update a profile; passwords and verification cannot be written', async () => {
  const ref = doc(db('alice'), 'users/alice');
  await assertSucceeds(setDoc(ref, profile));
  await assertSucceeds(getDoc(ref));
  await assertSucceeds(updateDoc(ref, { name: 'Updated Farmer' }));
  await assertFails(updateDoc(ref, { password: 'never-store-this' }));
  await assertFails(updateDoc(ref, { isVerified: true }));
  await assertFails(updateDoc(ref, { role: 'admin' }));
  await assertFails(updateDoc(ref, { name: 'x'.repeat(101) }));
});

test('anonymous requests and other accounts cannot read or write private profiles or nested data', async () => {
  await setDoc(doc(db('alice'), 'users/alice'), profile);
  await setDoc(doc(db('alice'), 'users/alice/produce/p1'), produce);
  for (const uid of [null, 'bob']) {
    await assertFails(getDoc(doc(db(uid), 'users/alice')));
    await assertFails(setDoc(doc(db(uid), 'users/alice'), profile));
    await assertFails(getDoc(doc(db(uid), 'users/alice/produce/p1')));
    await assertFails(deleteDoc(doc(db(uid), 'users/alice/produce/p1')));
  }
  await assertFails(setDoc(doc(db('alice'), 'users/alice/arbitrary/doc'), { value: true }));
});

test('produce create/delete and bookmark CRUD; malformed data and status forgery denied', async () => {
  const database = db('alice');
  const ref = doc(database, 'users/alice/produce/p1');
  await assertSucceeds(setDoc(ref, produce));
  await assertFails(updateDoc(ref, { status: 'DELIVERED' }));
  await assertFails(setDoc(doc(database, 'users/alice/produce/invalid'), { ...produce, id: 'invalid', quantity: -1 }));
  await assertSucceeds(deleteDoc(ref));
  const bookmark = doc(database, 'users/alice/bookmarks/pm_tractor');
  await assertSucceeds(setDoc(bookmark, { schemeId: 'pm_tractor' }));
  await assertSucceeds(getDoc(bookmark));
  await assertFails(setDoc(bookmark, { schemeId: 'different' }));
  await assertSucceeds(deleteDoc(bookmark));
});

test('catalogs are readable after login and writable only by a trusted admin', async () => {
  const admin = environment.authenticatedContext('publisher', { admin: true }).firestore();
  await assertSucceeds(setDoc(doc(admin, 'buyers/b1'), { id: 'b1', name: 'Test Buyer' }));
  await assertSucceeds(getDoc(doc(db('alice'), 'buyers/b1')));
  await assertFails(getDoc(doc(db(null), 'buyers/b1')));
  await assertFails(setDoc(doc(db('alice'), 'buyers/fake'), { isVerified: true }));
  await assertFails(setDoc(doc(db('alice'), 'notices/fake'), { authorityEn: 'Government' }));
});

test('buyer selection must use published data; delivery creation is atomic and cannot forge progress', async () => {
  const database = db('alice');
  const listing = doc(database, 'users/alice/produce/p1');
  await setDoc(listing, produce);
  await assertFails(updateDoc(listing, { status: 'BUYER_SELECTED', selectedBuyer: { id: 'fake', isVerified: true } }));
  const delivery = { id: 'd1', produceListingId: 'p1', produceTitle: 'Wheat', quantity: '10 Qtl', pickupLocation: 'Farm',
    destination: 'Mandi', pickupDate: '2026-10-14', pickupTime: '10:00', vehicleType: 'Tractor', isSelfTransport: true,
    estimatedFare: 0, additionalInstructions: '', status: 'REQUESTED', statusUpdatedAt: 'Now', trackingUpdates: [] };
  const deliveryRef = doc(database, 'users/alice/deliveries/d1');
  await assertFails(setDoc(deliveryRef, delivery));
  const batch = writeBatch(database);
  batch.set(deliveryRef, delivery);
  batch.update(listing, { status: 'DELIVERY_REQUESTED', deliveryRequest: delivery });
  await assertSucceeds(batch.commit());
  await assertFails(updateDoc(deliveryRef, { status: 'DELIVERED' }));
  await assertFails(deleteDoc(listing));
});

test('bookings require real catalog equipment; owners may cancel but not confirm or change price', async () => {
  await environment.withSecurityRulesDisabled(async context => setDoc(doc(context.firestore(), 'equipment/e1'), { id: 'e1' }));
  const ref = doc(db('alice'), 'users/alice/bookings/b1');
  const booking = { id: 'b1', equipmentId: 'e1', equipmentName: 'Tractor', providerName: 'Provider', bookingDate: '2026-10-14',
    timeSlot: 'Morning', durationOrArea: '2 hours', farmLocation: 'Farm', instructions: '', estimatedCost: 500,
    status: 'REQUESTED', statusDate: 'Today', providerPhone: '', iconEmoji: '' };
  await assertSucceeds(setDoc(ref, booking));
  await assertFails(updateDoc(ref, { status: 'ACCEPTED' }));
  await assertFails(updateDoc(ref, { estimatedCost: 1 }));
  await assertSucceeds(updateDoc(ref, { status: 'CANCELLED' }));
  await assertFails(updateDoc(ref, { status: 'REQUESTED' }));
});

test('applications and grievances are private saved records with no forged official acceptance', async () => {
  const ref = doc(db('alice'), 'users/alice/applications/a1');
  const application = { id: 'a1', appId: 'a1', schemeTitle: 'Scheme', schemeTitleHi: '', appliedDate: '2026-10-13',
    status: 'Saved — submit through official portal', progressPercent: 0, applicantName: 'Farmer', phone: '9876543210', aadhaarLastFour: '1234' };
  await assertSucceeds(setDoc(ref, application));
  await assertFails(updateDoc(ref, { status: 'Approved' }));
  await assertFails(getDoc(doc(db('bob'), 'users/alice/applications/a1')));
  await assertFails(setDoc(doc(db('alice'), 'users/alice/applications/a2'), { ...application, id: 'a2', appId: 'a2', aadhaar: '123456789012' }));
  await assertSucceeds(setDoc(doc(db('alice'), 'users/alice/grievances/g1'), { id: 'g1', category: 'Water', subject: 'Broken pipe',
    description: 'Test report', applicantName: 'Farmer', phone: '', date: 'Today', status: 'Saved — pending official submission' }));
});

test('Firebase Auth emulator registration, logout, invalid password, login, and reset email', async () => {
  const app = initializeApp({ apiKey: 'demo-key', projectId: 'demo-digital-saathi' }, `auth-${Date.now()}`);
  try {
    const auth = getAuth(app);
    connectAuthEmulator(auth, 'http://127.0.0.1:9099', { disableWarnings: true });
    const email = `farmer-${Date.now()}@example.com`;
    const first = await createUserWithEmailAndPassword(auth, email, 'test-password-123');
    const uid = first.user.uid;
    await signOut(auth);
    assert.equal(auth.currentUser, null);
    await assert.rejects(signInWithEmailAndPassword(auth, email, 'incorrect-password'));
    const second = await signInWithEmailAndPassword(auth, email, 'test-password-123');
    assert.equal(second.user.uid, uid);
    await sendPasswordResetEmail(auth, email);
    await signOut(auth);
  } finally { await deleteApp(app); }
});
