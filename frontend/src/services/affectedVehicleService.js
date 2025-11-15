// src/services/affectedVehicleService.js
import { authHeaders, handleJson } from './httpCore.js';

export async function listAffectedByCampaign(campaignId, { page = 0, size = 50 } = {}) {
  // giả định endpoint: GET /api/v1/affected-vehicles/by-campaign/{id}
  const base = '/api/v1/affected-vehicles';
  const params = new URLSearchParams({ page, size });
  const res = await fetch(`${base}/by-campaign/${campaignId}?${params.toString()}`, {
    headers: authHeaders()
  });
  const pageData = await handleJson(res);
  return pageData?.content ?? []; // trả về mảng
}
