// src/JS/sc-vehicle-history.js
import { getVehicleByVin, getVehicleHistory } from '../services/vehicleService.js';

export async function initVehicleHistoryPage() {
  const params = new URLSearchParams(window.location.search);
  const vin = params.get('vin') || '';

  const vinInput = document.getElementById('vh-vin-input');
  const vinBtn   = document.getElementById('vh-search-btn');
  vinInput.value = vin;

  vinBtn.addEventListener('click', () => {
    const v = vinInput.value.trim();
    if (v) window.location.search = `?vin=${encodeURIComponent(v)}`;
  });

  document.getElementById('vh-filter-btn').addEventListener('click', () => {
    if (!vinInput.value.trim()) return;
    renderHistory(vinInput.value.trim());
  });

  if (!vin) return;
  await renderVehicleInfo(vin);
  await renderHistory(vin);
}

async function renderVehicleInfo(vin) {
  try {
    const v = await getVehicleByVin(vin);
    setText('vh-model', v?.model?.name ?? v?.model ?? '--');
    setText('vh-year', v?.year ?? '--');
    setText('vh-battery', v?.batteryType ?? '--');
    setText('vh-odo', v?.odo ? v.odo.toLocaleString('vi-VN') + ' km' : '--');

    setText('vh-owner', v?.ownerName ?? '--');
    setText('vh-phone', v?.ownerPhone ?? '--');
    setText('vh-email', v?.ownerEmail ?? '--');
    setText('vh-address', v?.ownerAddress ?? '--');
  } catch (e) {
    console.error(e);
    alert('Không tải được thông tin xe');
  }
}

async function renderHistory(vin) {
  const tbody = document.getElementById('vh-tbody');
  const empty = document.getElementById('vh-empty');
  tbody.innerHTML = '';
  empty.classList.add('d-none');

  const filters = {
    type: document.getElementById('vh-type').value || '',
    center: document.getElementById('vh-center').value || '',
    from: document.getElementById('vh-from').value || '',
    to: document.getElementById('vh-to').value || ''
  };

  let list = [];
  try {
    list = await getVehicleHistory(vin, filters);
  } catch {
    list = []; // fallback
  }

  if (!list || list.length === 0) {
    empty.classList.remove('d-none');
    return;
  }

  for (const it of list) {
    const tr = document.createElement('tr');
    const dateStr = it.date ? new Date(it.date).toLocaleDateString('vi-VN') : '--';
    tr.innerHTML = `
      <td>${dateStr}</td>
      <td>${mapType(it.type)}</td>
      <td>${it.centerName ?? '—'}</td>
      <td><span class="badge ${mapStatusClass(it.status)}">${it.status ?? 'N/A'}</span></td>
    `;
    tbody.appendChild(tr);
  }
}

function setText(id, val) {
  const el = document.getElementById(id);
  if (el) el.textContent = val ?? '--';
}
function mapType(t) {
  switch ((t || '').toUpperCase()) {
    case 'MAINTENANCE': return 'Bảo dưỡng định kỳ';
    case 'RECALL':      return 'Chiến dịch/Recall';
    case 'REPAIR':      return 'Sửa chữa';
    default:            return 'Khác';
  }
}
function mapStatusClass(s) {
  switch ((s || '').toUpperCase()) {
    case 'COMPLETED': case 'DONE': return 'bg-success';
    case 'SCHEDULED':              return 'bg-info';
    case 'PENDING':                return 'bg-secondary';
    case 'REJECTED':               return 'bg-danger';
    default:                       return 'bg-secondary';
  }
}
