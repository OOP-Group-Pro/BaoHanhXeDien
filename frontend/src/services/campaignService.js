// src/services/campaignService.js
import { authHeaders, handlePageJson, handleJson } from './httpCore.js';

const BASE = '/api/v1/campaigns';

export async function searchCampaigns({ code, status, type, page = 0, size = 20 } = {}) {
  const params = new URLSearchParams();
  if (code) params.set('code', code);
  if (status) params.set('status', status);
  if (type) params.set('type', type);
  params.set('page', page); params.set('size', size);

  const res = await fetch(`${BASE}?${params.toString()}`, { headers: authHeaders() });
  return handlePageJson(res);
}

export async function createCampaign(payload) {
  const res = await fetch(BASE, {
    method: 'POST',
    headers: { ...authHeaders(), 'Content-Type': 'application/json' },
    body: JSON.stringify(payload),
  });
  return handleJson(res);
}

export async function updateCampaign(id, payload) {
  const res = await fetch(`${BASE}/${id}`, {
    method: 'PUT',
    headers: { ...authHeaders(), 'Content-Type': 'application/json' },
    body: JSON.stringify(payload),
  });
  return handleJson(res);
}

export async function getCampaign(id) {
  const res = await fetch(`${BASE}/${id}`, { headers: authHeaders() });
  return handleJson(res);
}

export async function deleteCampaign(id) {
  const res = await fetch(`${BASE}/${id}`, { method: 'DELETE', headers: authHeaders() });
  if (!res.ok) throw new Error('Xóa thất bại');
}
