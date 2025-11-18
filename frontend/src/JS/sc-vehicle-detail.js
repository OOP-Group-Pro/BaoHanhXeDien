// ====== LOGIC TRANG CHI TIẾT XE ======
import { getVehicleByVin, getVehicleHistory } from '../services/vehicleService.js';

export async function initVehicleDetailPage() {
  // Lấy VIN từ query (?vin=)
  const params = new URLSearchParams(window.location.search);
  const vinFromQuery = params.get('vin') || '';
  // bind UI
  const vinInput = document.getElementById('vin-input');
  const vinSearchBtn = document.getElementById('vin-search-btn');
  vinInput.value = vinFromQuery;

  vinSearchBtn.addEventListener('click', () => {
    const v = vinInput.value.trim();
    if (v) window.location.search = `?vin=${encodeURIComponent(v)}`;
  });

  if (!vinFromQuery) return;

  try {
  // 1) Tải thông tin xe
    const v = await getVehicleByVin(vinFromQuery);
    setText('vd-vin', v.vin || vinFromQuery);
    setText('vd-model', v.model ? `${v.model.name || v.model} (${v.year || ''})` : '--');
    setText('vd-year', v.year ?? '--');
    setText('vd-owner', v.ownerName ?? '--');
    setText('vd-phone', v.ownerPhone ?? '--');
    setText('vd-address', v.ownerAddress ?? '--');

    // 2) Tải lịch sử
    await loadHistory(vinFromQuery);

    // filter theo loại
    document.getElementById('history-type-filter')
      .addEventListener('change', async (e) => {
        await loadHistory(vinFromQuery, e.target.value);
      });
  } catch (err) {
    console.error(err);
    alert(err.message);
  }
}

async function loadHistory(vin, type = '') {
  const tbody = document.getElementById('history-tbody');
  const emptyEl = document.getElementById('history-empty');
  tbody.innerHTML = '';
  emptyEl.classList.add('d-none');

// Gọi API thật:
  let items = [];
  try {
    items = await getVehicleHistory(vin, { type });
  } catch (_) {
    items = [];
  }
  if (!items.length) {
    emptyEl.classList.remove('d-none');
    return;
  }

  for (const it of items) {
    const tr = document.createElement('tr');
    const dateStr = it.date ? new Date(it.date).toLocaleDateString('vi-VN') : '--';
    const typeLabel = mapServiceType(it.type);
    const statusBadge = `<span class="badge bg-${mapStatusColor(it.status)}">${it.status || 'N/A'}</span>`;
    tr.innerHTML = `
      <td>${dateStr}</td>
      <td>${typeLabel}</td>
      <td>${it.centerName || '—'}</td>
      <td>${statusBadge}</td>`;
    tbody.appendChild(tr);
  }
}

function setText(id, value) {
  const el = document.getElementById(id);
  if (el) el.textContent = value ?? '--';
}
function mapServiceType(t) {
  switch ((t || '').toUpperCase()) {
    case 'MAINTENANCE': return 'Bảo dưỡng định kỳ';
    case 'RECALL':      return 'Chiến dịch/Recall';
    case 'REPAIR':      return 'Sửa chữa';
    default:            return 'Khác';
  }
}
function mapStatusColor(s) {
// tuỳ chỉnh badge màu
  switch ((s || '').toUpperCase()) {
    case 'DONE':
    case 'COMPLETED': return 'success';
    case 'SCHEDULED': return 'info';
    case 'PENDING':   return 'secondary';
    case 'REJECTED':  return 'danger';
    default:          return 'secondary';
  }
}
