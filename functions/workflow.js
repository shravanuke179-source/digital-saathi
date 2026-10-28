export const deliveryTransitions = Object.freeze({
  REQUESTED: 'ACCEPTED', ACCEPTED: 'PICKUP_SCHEDULED', PICKUP_SCHEDULED: 'PICKED_UP',
  PICKED_UP: 'IN_TRANSIT', IN_TRANSIT: 'DELIVERED'
});
export const produceStatus = Object.freeze({ ACCEPTED: 'DELIVERY_ACCEPTED', PICKUP_SCHEDULED: 'PICKUP_SCHEDULED',
  PICKED_UP: 'PICKED_UP', IN_TRANSIT: 'IN_TRANSIT', DELIVERED: 'DELIVERED' });

export function validateDeliveryUpdate(data) {
  if (!data || typeof data !== 'object' || Array.isArray(data) ||
      Object.keys(data).some(key => !['uid', 'deliveryId', 'status'].includes(key))) throw new Error('Invalid request fields.');
  for (const key of ['uid', 'deliveryId']) {
    if (typeof data[key] !== 'string' || !/^[A-Za-z0-9_-]{1,128}$/.test(data[key])) throw new Error(`Invalid ${key}.`);
  }
  if (typeof data.status !== 'string' || !Object.hasOwn(produceStatus, data.status)) throw new Error('Invalid delivery status.');
  return data;
}

export function assertNextStatus(current, next) {
  if (deliveryTransitions[current] !== next) throw new Error('Invalid delivery transition.');
}
