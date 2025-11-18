const BASE = ''; // để trống nếu gateway cùng origin

export async function fetchOverview() {
  try {
    const res = await fetch(`${BASE}/api/v1/claims/metrics/overview`);
    if (!res.ok) throw new Error('metrics fail');
    return await res.json();
  } catch {
    // MOCK cho UI khi backend chưa có
    return {
      totalCost: 2_510_000_000,
      totalClaims: 45,
      ratios: { approved: 0.88, reviewing: 0.12, rejected: 0.12 },
      statusBreakdown: { APPROVED: 56, REVIEWING: 24, REJECTED: 8, PENDING: 12 }
    };
  }
}

export async function fetchMonthlyCosts(months = 6) {
  try {
    const res = await fetch(`${BASE}/api/v1/costs/warranty/monthly?months=${months}`);
    if (!res.ok) throw new Error('monthly fail');
    return await res.json(); // [{month:'MM/YYYY', cost:number}, ...]
  } catch {
    const now = new Date();
    const out = [];
    for (let i = months - 1; i >= 0; i--) {
      const d = new Date(now.getFullYear(), now.getMonth() - i, 1);
      const label = `${String(d.getMonth()+1).padStart(2,'0')}/${d.getFullYear()}`;
      out.push({ month: label, cost: Math.round((Math.random()*0.08 + 0.02) * 1_000_000_000) });
    }
    return out;
  }
}
