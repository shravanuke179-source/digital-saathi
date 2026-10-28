import { test } from 'node:test';
import assert from 'node:assert/strict';
import { validateDeliveryUpdate, assertNextStatus } from './workflow.js';

test('rejects path injection, missing/extra fields and unsupported statuses', () => {
  for (const input of [null, {}, [], { uid: '../victim', deliveryId: 'd1', status: 'ACCEPTED' },
    { uid: 'u1', deliveryId: 'd1', status: 'DELIVERED', admin: true },
    { uid: 'u1', deliveryId: 'd1', status: 'COMPLETED' },
    { uid: 'u1', deliveryId: 'd1', status: ['ACCEPTED'] }]) {
    assert.throws(() => validateDeliveryUpdate(input));
  }
  assert.deepEqual(validateDeliveryUpdate({ uid: 'u1', deliveryId: 'DEL-123', status: 'ACCEPTED' }),
    { uid: 'u1', deliveryId: 'DEL-123', status: 'ACCEPTED' });
});

test('delivery workflow requires each transition and rejects replay or terminal changes', () => {
  assert.doesNotThrow(() => assertNextStatus('REQUESTED', 'ACCEPTED'));
  assert.doesNotThrow(() => assertNextStatus('IN_TRANSIT', 'DELIVERED'));
  assert.throws(() => assertNextStatus('REQUESTED', 'DELIVERED'));
  assert.throws(() => assertNextStatus('ACCEPTED', 'ACCEPTED'));
  assert.throws(() => assertNextStatus('DELIVERED', 'ACCEPTED'));
});
