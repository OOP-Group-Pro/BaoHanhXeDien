// src/JS/evm-campaigns.js
import { getUser } from '../utils/storage.js';
import { toast } from '../utils/main.js'; // nếu chưa có toast, tạm thay bằng alert
import {
  searchCampaigns, createCampaign, updateCampaign, deleteCampaign, getCampaign
} from '../services/campaignService.js';
import { listAffectedByCampaign } from '../services/affectedVehicleService.js';

const fmt = (n) => n?.toLocaleString('vi-VN') ?? '—';
const $ = (id) => document.getElementById(id);

let state = {
  list: [],
  selectedId: null,
};

export async function initEvmCampaignsPage() {
  // bindings
  $('#cpn-new-btn').addEventListener('click', openCreateModal);
  $('#cpn-filter-status').addEventListener('change', reload);
  $('#cpn-modal-close').addEventListener('click', closeModal);
  $('#cpn-cancel-btn').addEventListener('click', closeModal);
  $('#cpn-save-btn').addEventListener('click', onSaveModal);
  $('#cpn-edit-btn').addEventListener('click', onEditSelected);
  $('#cpn-del-btn').addEventListener('click', onDeleteSelected);

  await reload();
}

async function reload() {
  const status = $('#cpn-filter-status').value || undefined;
  const page = await searchCampaigns({ status, size: 50 });
  state.list = page?.content ?? [];
  renderList();
  renderKpis();
  if (state.selectedId) selectCampaign(state.selectedId, { silent: true });
}

function renderKpis() {
  const count = state.list.length;
  const active = state.list.filter(i => i.status === 'ACTIVE').length;
  // Tổng chi phí: nếu backend chưa có field, mình tạm hiển thị số lượng xe ảnh hưởng * placeholder
  $('#cpn-total-cost').textContent = '—';
  $('#cpn-count').textContent = fmt(count);
  $('#cpn-active').textContent = fmt(active);
}

function renderList() {
  const box = $('#cpn-list');
  const empty = $('#cpn-empty');
  box.innerHTML = '';

  if (!state.list.length) {
    empty.classList.remove('d-none');
    return;
  }
  empty.classList.add('d-none');

  for (const c of state.list) {
    const div = document.createElement('div');
    div.className = 'list-item';
    div.innerHTML = `
      <div>
        <div class="list-title">${c.code} <span class="badge">${c.status}</span></div>
        <div class="muted">${c.title ?? ''}</div>
        <small class="muted">${fmtTime(c.startAt)} → ${fmtTime(c.endAt)}</small>
      </div>
    `;
    div.addEventListener('click', () => selectCampaign(c.id));
    if (c.id === state.selectedId) div.classList.add('active');
    box.appendChild(div);
  }
}

async function selectCampaign(id, { silent } = {}) {
  state.selectedId = id;
  renderList();

  const data = await getCampaign(id);
  const detailBox = $('#cpn-selected');
  const emptyBox = $('#cpn-selected-empty');

  if (!data) {
    detailBox.classList.add('d-none');
    emptyBox.classList.remove('d-none');
    return;
  }
  emptyBox.classList.add('d-none');
  detailBox.classList.remove('d-none');

  $('#cpn-code').textContent = data.code;
  $('#cpn-title').textContent = data.title;
  $('#cpn-type').textContent = data.type;
  $('#cpn-status').textContent = data.status;
  $('#cpn-time').textContent = `${fmtTime(data.startAt)} → ${fmtTime(data.endAt)}`;

  await renderAffected(id);
  if (!silent) toast && toast('Đã tải chi tiết chiến dịch');
}

async function renderAffected(campaignId) {
  const tbody = $('#av-tbody');
  const empty = $('#av-empty');
  tbody.innerHTML = '';
  let list = [];

  try {
    list = await listAffectedByCampaign(campaignId);
  } catch {
    list = [];
  }
  if (!list.length) {
    empty.classList.remove('d-none');
    return;
  }
  empty.classList.add('d-none');

  for (const v of list) {
    const tr = document.createElement('tr');
    tr.innerHTML = `
      <td>${v.id ?? '—'}</td>
      <td>${v.vehicleVin ?? v.vin ?? '—'}</td>
      <td><span class="badge">${v.status ?? '—'}</span></td>
      <td>
        <button class="btn sm" data-vin="${v.vin ?? v.vehicleVin}">Chi tiết</button>
      </td>
    `;
    tbody.appendChild(tr);
  }
}

/* ============ modal ============ */
function openCreateModal() {
  $('#cpn-modal-title').textContent = 'Tạo Chiến dịch';
  fillModal(); // clear
  $('#cpn-modal').classList.remove('d-none');
}
function openEditModal(data) {
  $('#cpn-modal-title').textContent = 'Sửa Chiến dịch';
  fillModal(data);
  $('#cpn-modal').classList.remove('d-none');
}
function closeModal() {
  $('#cpn-modal').classList.add('d-none');
}
function fillModal(d = {}) {
  $('#m-code').value = d.code ?? '';
  $('#m-title').value = d.title ?? '';
  $('#m-type').value = d.type ?? 'RECALL';
  $('#m-start').value = toLocalInput(d.startAt) ?? '';
  $('#m-end').value = toLocalInput(d.endAt) ?? '';
}
async function onSaveModal() {
  const payload = {
    code: $('#m-code').value.trim(),
    title: $('#m-title').value.trim(),
    type: $('#m-type').value,
    startAt: fromLocalInput($('#m-start').value),
    endAt: fromLocalInput($('#m-end').value),
  };
  if (!payload.code || !payload.title || !payload.startAt || !payload.endAt) {
    alert('Vui lòng nhập đủ thông tin.');
    return;
  }

  if (!state.selectedId || $('#cpn-modal-title').textContent.includes('Tạo')) {
    await createCampaign(payload);
    toast && toast('Đã tạo chiến dịch');
  } else {
    await updateCampaign(state.selectedId, payload);
    toast && toast('Đã cập nhật chiến dịch');
  }
  closeModal();
  await reload();
}

async function onEditSelected() {
  if (!state.selectedId) return;
  const data = await getCampaign(state.selectedId);
  if (data) openEditModal(data);
}
async function onDeleteSelected() {
  if (!state.selectedId) return;
  if (!confirm('Xóa chiến dịch này?')) return;
  await deleteCampaign(state.selectedId);
  state.selectedId = null;
  await reload();
}

/* ============ helpers ============ */
function fmtTime(s) {
  if (!s) return '—';
  try { return new Date(s).toLocaleString('vi-VN'); } catch { return s; }
}
function toLocalInput(iso) {
  if (!iso) return '';
  const d = new Date(iso);
  // yyyy-MM-ddTHH:mm for <input type="datetime-local">
  const pad = (n) => String(n).padStart(2, '0');
  return `${d.getFullYear()}-${pad(d.getMonth()+1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`;
}
function fromLocalInput(v) {
  return v ? new Date(v).toISOString() : null;
}
