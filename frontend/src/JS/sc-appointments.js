// src/JS/sc-appointments.js
import { getAppointments, approveAppointment, rejectAppointment } from '../services/appointmentService.js';
import { renderHeader } from '../components/Header.js';
import { renderStaffSidebar } from '../components/StaffSidebar.js';
import { checkAuth } from '../utils/auth.js';

let current = new Date(); // tháng đang xem

export async function initAppointmentsPage() {
  await checkAuth(['SC_STAFF']); // tuỳ theo role bạn dùng
  renderHeader('SC Staff');
  renderStaffSidebar('appointments');

  bindNav();
  await drawMonth();
  await loadPendingAndUpcoming();
}

function bindNav() {
  document.getElementById('ap-prev').onclick = async () => { current.setMonth(current.getMonth()-1); await drawMonth(); };
  document.getElementById('ap-next').onclick = async () => { current.setMonth(current.getMonth()+1); await drawMonth(); };
  document.getElementById('ap-today').onclick = async () => { current = new Date(); await drawMonth(); };
  document.getElementById('ap-new-btn').onclick = () => {
    // Điều hướng tới trang tạo lịch hẹn (nếu có) hoặc mở modal
    alert('TODO: mở form tạo lịch hẹn');
  };
}

async function drawMonth() {
  const label = document.getElementById('ap-month-label');
  label.textContent = `${current.toLocaleString('vi-VN',{month:'long'})} ${current.getFullYear()}`;

  const first = new Date(current.getFullYear(), current.getMonth(), 1);
  const start = new Date(first);
  start.setDate(start.getDate() - start.getDay()); // về Chủ nhật

  const grid = document.getElementById('ap-grid');
  grid.innerHTML = '';

  // lấy khoảng ngày 6 hàng x 7 cột
  const days = [];
  for (let i=0;i<42;i++) {
    const d = new Date(start);
    d.setDate(start.getDate()+i);
    days.push(d);
  }

  // gọi API lấy lịch hẹn trong khoảng hiển thị
  const from = isoDate(days[0]) + 'T00:00:00';
  const to   = isoDate(days[41]) + 'T23:59:59';
  let appts = [];
  try {
    appts = await getAppointments({ from, to });
  } catch (e) {
    // nếu backend chưa sẵn: mock nhẹ
    appts = [];
  }

  for (const d of days) {
    const cell = document.createElement('div');
    cell.className = 'day-cell';
    if (isSameDate(d, new Date())) cell.classList.add('today');

    const n = document.createElement('div');
    n.className = 'd';
    n.textContent = d.getDate();
    cell.appendChild(n);

    // render event của ngày d
    const dayAppts = appts.filter(a => sameDayStr(a.scheduledAt, d));
    for (const a of dayAppts) {
      const span = document.createElement('span');
      span.className = 'event ' + (a.status || '').toLowerCase();
      const hhmm = toHHMM(a.scheduledAt);
      span.textContent = `${hhmm} • ${a.customerName || a.vin || ''}`;
      span.title = `${a.centerName || ''} • ${a.vin || ''}`;
      cell.appendChild(span);
    }

    grid.appendChild(cell);
  }
}

async function loadPendingAndUpcoming() {
  let list = [];
  try {
    const now = new Date();
    const to = new Date(now); to.setDate(now.getDate()+30);
    list = await getAppointments({
      from: isoDate(now)+'T00:00:00',
      to:   isoDate(to)+'T23:59:59',
      status: 'PENDING'
    });
  } catch {}
  const wrap = document.getElementById('ap-pending');
  wrap.innerHTML = list.length ? '' : '<div class="muted">Không có lịch hẹn chờ duyệt.</div>';

  for (const it of list) {
    const div = document.createElement('div');
    div.className = 'pending-item';
    div.innerHTML = `
      <div><b>${it.customerName || 'Khách'}</b> – VIN: ${it.vin || ''}</div>
      <div class="muted">${new Date(it.scheduledAt).toLocaleString('vi-VN')} • ${it.centerName || ''}</div>
      <div class="actions">
        <button class="btn xs success">Xác nhận</button>
        <button class="btn xs danger">Huỷ bỏ</button>
      </div>
    `;
    const [btnOk, btnCancel] = div.querySelectorAll('button');
    btnOk.onclick = async () => { await approveAppointment(it.id); await drawMonth(); await loadPendingAndUpcoming(); };
    btnCancel.onclick = async () => { await rejectAppointment(it.id); await drawMonth(); await loadPendingAndUpcoming(); };
    wrap.appendChild(div);
  }

  // upcoming (đơn giản): đếm tổng số đã xác nhận trong 7 ngày tới
  const up = document.getElementById('ap-upcoming');
  up.textContent = 'Tác nhân: Xác duyệt / Theo tuần tới (demo)';
}

/* helpers */
function isoDate(d){ return (d instanceof Date ? d : new Date(d)).toISOString().slice(0,10); }
function toHHMM(d){ const t = new Date(d); return String(t.getHours()).padStart(2,'0')+':'+String(t.getMinutes()).padStart(2,'0'); }
function isSameDate(a,b){ return isoDate(a)===isoDate(b); }
function sameDayStr(a, d){ return isoDate(a)===isoDate(d); }
