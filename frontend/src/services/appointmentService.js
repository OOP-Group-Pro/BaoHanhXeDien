// src/services/appointmentService.js
import { API_BASE } from '../utils/storage.js'; // chỉnh đường dẫn nếu khác

const BASE = `${API_BASE}/api/v1/appointments`;

/* ========= Helpers ========= */
function q(params = {}) {
  const sp = new URLSearchParams();
  Object.entries(params).forEach(([k, v]) => {
    if (v !== undefined && v !== null && v !== '') sp.set(k, v);
  });
  return sp.toString() ? `?${sp.toString()}` : '';
}

async function jsonFetch(url, opts = {}) {
  const res = await fetch(url, {
    credentials: 'include',
    headers: { 'Content-Type': 'application/json', ...(opts.headers || {}) },
    ...opts,
  });
  if (!res.ok) {
    const text = await res.text().catch(() => '');
    throw new Error(text || `HTTP ${res.status}`);
  }
  // DELETE thường không trả body
  if (res.status === 204) return true;
  const ct = res.headers.get('content-type') || '';
  return ct.includes('application/json') ? res.json() : res.text();
}

/* ========= API chính ========= */

// POST /api/v1/appointments
export function createAppointment(payload) {
  // payload: { campaignId, affectedId, scheduledAt, serviceCenterId, note? }
  return jsonFetch(BASE, { method: 'POST', body: JSON.stringify(payload) });
}

// GET /api/v1/appointments/{id}
export function getAppointment(id) {
  return jsonFetch(`${BASE}/${id}`);
}

// DELETE /api/v1/appointments/{id}
export function deleteAppointment(id) {
  return jsonFetch(`${BASE}/${id}`, { method: 'DELETE' });
}

// POST /api/v1/appointments/{id}/reschedule
export function rescheduleAppointment(id, payload) {
  // payload: { scheduledAt, serviceCenterId, note? }
  return jsonFetch(`${BASE}/${id}/reschedule`, {
    method: 'POST',
    body: JSON.stringify(payload),
  });
}

// POST /api/v1/appointments/{id}/complete
export function completeAppointment(id, payload) {
  // payload: { outcome, note? }  (theo AppointmentCompleteRequest)
  return jsonFetch(`${BASE}/${id}/complete`, {
    method: 'POST',
    body: JSON.stringify(payload),
  });
}

/* ========= Listing ========= */

// GET /api/v1/appointments/by-campaign/{campaignId}?status=&page=&size=&sort=
export function listAppointmentsByCampaign(campaignId, { status, page, size, sort } = {}) {
  return jsonFetch(
    `${BASE}/by-campaign/${campaignId}${q({ status, page, size, sort })}`
  );
}

// GET /api/v1/appointments/by-affected/{affectedId}?page=&size=&sort=
export function listAppointmentsByAffected(affectedId, { page, size, sort } = {}) {
  return jsonFetch(
    `${BASE}/by-affected/${affectedId}${q({ page, size, sort })}`
  );
}

/* ========= Kiểu status để dùng ở FE =========
   Theo enum backend:
   - SCHEDULED
   - RESCHEDULED
   - DONE
   - CANCELLED
*/
export const APPOINTMENT_STATUS = {
  SCHEDULED: 'SCHEDULED',
  RESCHEDULED: 'RESCHEDULED',
  DONE: 'DONE',
  CANCELLED: 'CANCELLED',
};
