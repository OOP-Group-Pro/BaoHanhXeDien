// Base URL của vehicle-service & warranty/appointment nếu cần
const VEHICLE_BASE = "http://localhost:8002/api/v1";        // vehicle-service
const WARRANTY_BASE = "http://localhost:8005/api/v1";       // warranty-service (nếu bạn serve lịch sử tại đây)
const CAMPAIGN_BASE = "http://localhost:8003/api/v1";       // campaign-service (nếu lịch sử có liên quan)

export async function getVehicleByVin(vin) {
  const res = await fetch(`${VEHICLE_BASE}/vehicles/${encodeURIComponent(vin)}`);
  if (!res.ok) throw new Error(`Không tìm thấy xe với VIN ${vin}`);
  return res.json();
}

/**
 * Tuỳ backend, bạn có thể:
 *  - gom lịch sử ở warranty-service: /vehicles/{vin}/history
 *  - hoặc tách 2 nguồn (appointment + claim), rồi merge phía FE.
 * Dưới đây là một phương án đơn giản (1 endpoint gộp).
 */
/**
 * filters: { type?, center?, from?, to? }
 * Backend (gợi ý): GET /api/v1/vehicles/{vin}/history?type=&center=&from=&to=
 */
export async function getVehicleHistory(vin, filters = {}) {
  const q = new URLSearchParams(filters).toString();
  const url = `${BASE}/vehicles/${encodeURIComponent(vin)}/history${q ? `?${q}` : ''}`;
  const res = await fetch(url);
  if (!res.ok) return [];
  return res.json();
}