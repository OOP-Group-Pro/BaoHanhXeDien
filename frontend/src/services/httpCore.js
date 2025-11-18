// src/services/httpCore.js
import { getUser } from '../utils/storage.js';

export function authHeaders() {
  const u = getUser?.();
  const h = { };
  if (u?.token) h.Authorization = `Bearer ${u.token}`;
  return h;
}
export async function handleJson(res) {
  if (!res.ok) {
    const text = await res.text();
    throw new Error(text || `HTTP ${res.status}`);
  }
  return res.status === 204 ? null : res.json();
}
export async function handlePageJson(res) { return handleJson(res); }
