// src/JS/evm-dashboard.js
import { fetchOverview, fetchMonthlyCosts } from '/src/services/evmService.js';
import { initEvmCampaignsPage } from './evm-campaigns.js';

(function main(){
  if (!document.getElementById('evm-dashboard-page')) return;
  loadDashboard();
})();

async function loadDashboard(){
  // KPI
  const ov = await fetchOverview();
  setText('kpi-total-claims', ov.totalClaims ?? '—');
  setText('kpi-total-cost', formatVND(ov.totalCost));
  const p = Math.min(Math.round((ov.totalCost || 0) / 3_000_000_000 * 100), 100);
  const bar = document.getElementById('kpi-progress');
  if (bar) bar.style.width = `${p}%`;

  const { approved=0, reviewing=0, rejected=0 } = ov.ratios || {};
  setText('kpi-approve',  toPct(approved));
  setText('kpi-review',   toPct(reviewing));
  setText('kpi-reject',   toPct(rejected));

  // Charts
  drawStatusChart(ov.statusBreakdown || {});
  const monthly = await fetchMonthlyCosts(6);
  drawMonthlyChart(monthly);
}

/* charts */
function drawStatusChart(b){
  const ctx = document.getElementById('chart-status'); if(!ctx) return;
  const labels = ['Đã duyệt','Đang xử lý','Từ chối','Chờ xử lý'];
  const data = [b.APPROVED||0, b.REVIEWING||0, b.REJECTED||0, b.PENDING||0];
  new Chart(ctx, {
    type: 'doughnut',
    data: { labels, datasets: [{ data }] },
    options: { responsive:true, plugins:{ legend:{ position:'bottom' } }, cutout:'60%' }
  });
}
function drawMonthlyChart(rows){
  const ctx = document.getElementById('chart-monthly'); if(!ctx) return;
  const labels = rows.map(r=>r.month);
  const data = rows.map(r=>r.cost/1_000_000); // Triệu VND
  new Chart(ctx, {
    type: 'line',
    data: { labels, datasets: [{ label:'Chi phí (triệu VND)', data, tension:0.35 }] },
    options: { responsive:true, scales:{ y:{ beginAtZero:true } }, plugins:{ legend:{ display:false } } }
  });
}

/* helpers */
function setText(id, v){ const el=document.getElementById(id); if(el) el.textContent=v; }
function formatVND(n){ return n==null ? '—' : n.toLocaleString('vi-VN',{style:'currency',currency:'VND',maximumFractionDigits:0}); }
function toPct(x){ return `${Math.round((x||0)*100)}%`; }
