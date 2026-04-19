<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Admin Panel — AuctionHub</title>
  <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700;800&display=swap" rel="stylesheet">
  <script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.2/dist/chart.umd.min.js"></script>
  <style>
    *, *::before, *::after { box-sizing: border-box; margin: 0; padding: 0; }

    :root {
      --bg:         #0a0e1a;
      --sidebar:    #0d1120;
      --card:       #111827;
      --card2:      #151d2e;
      --border:     rgba(99,102,241,0.18);
      --border2:    rgba(255,255,255,0.06);
      --accent:     #6366f1;
      --accent2:    #818cf8;
      --gold:       #f59e0b;
      --success:    #10b981;
      --danger:     #ef4444;
      --warn:       #f59e0b;
      --info:       #3b82f6;
      --text:       #f1f5f9;
      --muted:      #94a3b8;
      --muted2:     #64748b;
      --sidebar-w:  240px;
    }

    html { scroll-behavior: smooth; }

    body {
      font-family: 'Inter', sans-serif;
      background: var(--bg);
      color: var(--text);
      min-height: 100vh;
      display: flex;
    }

    /* ─── SIDEBAR ─────────────────────────────────────────────── */
    .sidebar {
      width: var(--sidebar-w);
      background: var(--sidebar);
      border-right: 1px solid var(--border);
      display: flex;
      flex-direction: column;
      position: fixed;
      top: 0; left: 0; bottom: 0;
      z-index: 100;
      overflow-y: auto;
    }

    .sidebar-brand {
      padding: 1.5rem 1.25rem 1rem;
      border-bottom: 1px solid var(--border);
    }

    .brand-logo {
      display: flex;
      align-items: center;
      gap: 0.6rem;
    }

    .brand-logo .ico {
      width: 36px; height: 36px;
      background: linear-gradient(135deg, #6366f1, #818cf8);
      border-radius: 9px;
      display: flex; align-items: center; justify-content: center;
      font-size: 1.1rem;
      box-shadow: 0 0 18px rgba(99,102,241,0.4);
      flex-shrink: 0;
    }

    .brand-logo .name {
      font-size: 1rem;
      font-weight: 800;
      color: var(--text);
      letter-spacing: -0.3px;
    }

    .brand-logo .sub {
      font-size: 0.65rem;
      color: var(--accent2);
      text-transform: uppercase;
      letter-spacing: 1px;
    }

    /* Admin info in sidebar */
    .admin-info {
      padding: 0.85rem 1.25rem;
      border-bottom: 1px solid var(--border);
      display: flex;
      align-items: center;
      gap: 0.6rem;
    }

    .admin-avatar {
      width: 34px; height: 34px;
      background: linear-gradient(135deg, var(--gold), #f97316);
      border-radius: 50%;
      display: flex; align-items: center; justify-content: center;
      font-size: 0.9rem;
      font-weight: 700;
      flex-shrink: 0;
    }

    .admin-info .aname { font-size: 0.82rem; font-weight: 600; color: var(--text); }
    .admin-info .arole {
      font-size: 0.68rem;
      color: var(--gold);
      background: rgba(245,158,11,0.1);
      border: 1px solid rgba(245,158,11,0.25);
      border-radius: 4px;
      padding: 1px 5px;
      margin-top: 1px;
      display: inline-block;
    }

    /* Nav links */
    .nav-section { padding: 1rem 1rem 0.25rem; }
    .nav-label {
      font-size: 0.62rem;
      text-transform: uppercase;
      letter-spacing: 1.2px;
      color: var(--muted2);
      font-weight: 600;
      padding: 0 0.25rem;
      margin-bottom: 0.4rem;
    }

    .nav-item {
      display: flex;
      align-items: center;
      gap: 0.7rem;
      padding: 0.6rem 0.8rem;
      border-radius: 9px;
      cursor: pointer;
      transition: all 0.18s;
      margin-bottom: 0.15rem;
      color: var(--muted);
      font-size: 0.84rem;
      font-weight: 500;
      border: none;
      background: none;
      width: 100%;
      text-align: left;
      text-decoration: none;
    }

    .nav-item:hover {
      background: rgba(99,102,241,0.08);
      color: var(--text);
    }

    .nav-item.active {
      background: rgba(99,102,241,0.15);
      color: var(--accent2);
      border-left: 2px solid var(--accent);
    }

    .nav-item .badge-count {
      margin-left: auto;
      background: rgba(99,102,241,0.2);
      color: var(--accent2);
      font-size: 0.68rem;
      font-weight: 700;
      padding: 1px 7px;
      border-radius: 20px;
    }

    .danger-badge-count {
      margin-left: auto;
      background: rgba(239,68,68,0.15);
      color: #fca5a5;
      font-size: 0.68rem;
      font-weight: 700;
      padding: 1px 7px;
      border-radius: 20px;
    }

    .sidebar-footer {
      margin-top: auto;
      padding: 1rem 1.25rem;
      border-top: 1px solid var(--border);
    }

    .btn-logout {
      display: flex;
      align-items: center;
      gap: 0.6rem;
      width: 100%;
      padding: 0.6rem 0.8rem;
      background: rgba(239,68,68,0.08);
      border: 1px solid rgba(239,68,68,0.18);
      border-radius: 9px;
      color: #fca5a5;
      font-size: 0.83rem;
      font-weight: 600;
      font-family: 'Inter', sans-serif;
      cursor: pointer;
      transition: all 0.18s;
      text-decoration: none;
    }

    .btn-logout:hover {
      background: rgba(239,68,68,0.15);
      border-color: rgba(239,68,68,0.35);
    }

    /* ─── MAIN CONTENT ──────────────────────────────────────────── */
    .main {
      margin-left: var(--sidebar-w);
      flex: 1;
      display: flex;
      flex-direction: column;
      min-height: 100vh;
    }

    /* Topbar */
    .topbar {
      padding: 1rem 1.75rem;
      border-bottom: 1px solid var(--border);
      display: flex;
      align-items: center;
      justify-content: space-between;
      background: rgba(13,17,32,0.8);
      backdrop-filter: blur(12px);
      position: sticky;
      top: 0;
      z-index: 50;
    }

    .topbar h2 {
      font-size: 1.1rem;
      font-weight: 700;
      color: var(--text);
    }

    .topbar .breadcrumb {
      font-size: 0.75rem;
      color: var(--muted);
      margin-top: 0.15rem;
    }

    .topbar-right { display: flex; align-items: center; gap: 0.75rem; }

    .status-dot {
      width: 8px; height: 8px;
      background: var(--success);
      border-radius: 50%;
      animation: pulse 2s ease infinite;
    }

    .status-label { font-size: 0.76rem; color: var(--success); font-weight: 500; }

    @keyframes pulse {
      0%, 100% { opacity: 1; transform: scale(1); }
      50%       { opacity: 0.6; transform: scale(1.2); }
    }

    /* Page content */
    .content {
      padding: 1.75rem;
      flex: 1;
    }

    /* ─── STATS GRID ─────────────────────────────────────────────── */
    .stats-grid {
      display: grid;
      grid-template-columns: repeat(4, 1fr);
      gap: 1rem;
      margin-bottom: 1.75rem;
    }

    .stat-card {
      background: var(--card);
      border: 1px solid var(--border);
      border-radius: 14px;
      padding: 1.2rem 1.25rem;
      position: relative;
      overflow: hidden;
      transition: transform 0.2s, box-shadow 0.2s;
    }

    .stat-card:hover {
      transform: translateY(-2px);
      box-shadow: 0 8px 25px rgba(0,0,0,0.3);
    }

    .stat-card::before {
      content: '';
      position: absolute;
      top: 0; left: 0; right: 0;
      height: 3px;
    }

    .stat-card.indigo::before  { background: linear-gradient(90deg,#6366f1,#818cf8); }
    .stat-card.green::before   { background: linear-gradient(90deg,#10b981,#34d399); }
    .stat-card.gold::before    { background: linear-gradient(90deg,#f59e0b,#fbbf24); }
    .stat-card.red::before     { background: linear-gradient(90deg,#ef4444,#f87171); }

    .stat-label {
      font-size: 0.73rem;
      color: var(--muted);
      text-transform: uppercase;
      letter-spacing: 0.8px;
      font-weight: 600;
      margin-bottom: 0.5rem;
    }

    .stat-value {
      font-size: 2rem;
      font-weight: 800;
      line-height: 1;
      margin-bottom: 0.3rem;
    }

    .stat-card.indigo .stat-value { color: var(--accent2); }
    .stat-card.green  .stat-value { color: #34d399; }
    .stat-card.gold   .stat-value { color: #fbbf24; }
    .stat-card.red    .stat-value { color: #f87171; }

    .stat-sub { font-size: 0.73rem; color: var(--muted2); }

    .stat-icon {
      position: absolute;
      right: 1rem;
      top: 50%;
      transform: translateY(-50%);
      font-size: 2.2rem;
      opacity: 0.12;
    }

    /* ─── TOAST ALERT ──────────────────────────────────────────── */
    .toast {
      display: flex;
      align-items: center;
      gap: 0.75rem;
      padding: 0.85rem 1.2rem;
      border-radius: 12px;
      margin-bottom: 1.25rem;
      font-size: 0.84rem;
      font-weight: 500;
      animation: slideIn 0.3s ease;
    }

    .toast.success {
      background: rgba(16,185,129,0.1);
      border: 1px solid rgba(16,185,129,0.25);
      color: #6ee7b7;
    }

    .toast.error {
      background: rgba(239,68,68,0.1);
      border: 1px solid rgba(239,68,68,0.25);
      color: #fca5a5;
    }

    @keyframes slideIn {
      from { opacity:0; transform: translateX(-12px); }
      to   { opacity:1; transform: translateX(0); }
    }

    /* ─── TABS ───────────────────────────────────────────────────── */
    .tab-bar {
      display: flex;
      gap: 0.25rem;
      background: var(--card);
      border: 1px solid var(--border);
      border-radius: 12px;
      padding: 0.3rem;
      margin-bottom: 1.25rem;
      width: fit-content;
    }

    .tab-btn {
      padding: 0.55rem 1.1rem;
      border-radius: 9px;
      border: none;
      background: none;
      color: var(--muted);
      font-size: 0.82rem;
      font-weight: 600;
      font-family: 'Inter', sans-serif;
      cursor: pointer;
      transition: all 0.18s;
      display: flex;
      align-items: center;
      gap: 0.4rem;
      white-space: nowrap;
    }

    .tab-btn:hover { color: var(--text); background: rgba(255,255,255,0.04); }

    .tab-btn.active {
      background: linear-gradient(135deg, rgba(99,102,241,0.25), rgba(99,102,241,0.15));
      color: var(--accent2);
      box-shadow: 0 0 0 1px rgba(99,102,241,0.3);
    }

    .tab-pill {
      background: rgba(99,102,241,0.2);
      color: var(--accent2);
      font-size: 0.65rem;
      padding: 1px 6px;
      border-radius: 10px;
      font-weight: 700;
    }

    .tab-content { display: none; }
    .tab-content.active-tab { display: block; }

    /* ─── TABLE SECTION ──────────────────────────────────────────── */
    .section-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin-bottom: 1rem;
    }

    .section-title {
      font-size: 1rem;
      font-weight: 700;
      color: var(--text);
      display: flex;
      align-items: center;
      gap: 0.5rem;
    }

    .section-title::after {
      content: '';
      height: 2px;
      width: 30px;
      background: linear-gradient(90deg, var(--accent), transparent);
      border-radius: 2px;
    }

    /* Search box */
    .search-box {
      position: relative;
    }

    .search-box input {
      background: rgba(255,255,255,0.04);
      border: 1px solid var(--border);
      border-radius: 9px;
      color: var(--text);
      font-size: 0.82rem;
      font-family: 'Inter', sans-serif;
      padding: 0.5rem 0.85rem 0.5rem 2.1rem;
      outline: none;
      transition: all 0.2s;
      width: 220px;
    }

    .search-box input:focus {
      border-color: var(--accent);
      background: rgba(99,102,241,0.06);
      box-shadow: 0 0 0 3px rgba(99,102,241,0.1);
    }

    .search-box .search-ico {
      position: absolute;
      left: 0.65rem;
      top: 50%;
      transform: translateY(-50%);
      font-size: 0.8rem;
      color: var(--muted);
      pointer-events: none;
    }

    /* Table container */
    .table-container {
      background: var(--card);
      border: 1px solid var(--border);
      border-radius: 14px;
      overflow: hidden;
    }

    table {
      width: 100%;
      border-collapse: collapse;
      font-size: 0.82rem;
    }

    thead {
      background: rgba(99,102,241,0.06);
      border-bottom: 1px solid var(--border);
    }

    thead th {
      padding: 0.85rem 1rem;
      text-align: left;
      font-size: 0.71rem;
      font-weight: 700;
      text-transform: uppercase;
      letter-spacing: 0.8px;
      color: var(--muted);
      white-space: nowrap;
    }

    tbody tr {
      border-bottom: 1px solid var(--border2);
      transition: background 0.15s;
    }

    tbody tr:last-child { border-bottom: none; }

    tbody tr:hover {
      background: rgba(99,102,241,0.04);
    }

    tbody td {
      padding: 0.8rem 1rem;
      color: var(--text);
      vertical-align: middle;
    }

    /* Badges */
    .badge {
      display: inline-flex;
      align-items: center;
      gap: 0.3rem;
      padding: 2px 8px;
      border-radius: 6px;
      font-size: 0.71rem;
      font-weight: 700;
      text-transform: uppercase;
      letter-spacing: 0.5px;
    }

    .badge-active  { background: rgba(16,185,129,0.12);  color: #34d399; border: 1px solid rgba(16,185,129,0.2); }
    .badge-closed  { background: rgba(99,102,241,0.12);  color: #818cf8; border: 1px solid rgba(99,102,241,0.2); }
    .badge-pending { background: rgba(245,158,11,0.12);  color: #fbbf24; border: 1px solid rgba(245,158,11,0.2); }
    .badge-paid    { background: rgba(16,185,129,0.12);  color: #34d399; border: 1px solid rgba(16,185,129,0.2); }
    .badge-failed  { background: rgba(239,68,68,0.12);   color: #f87171; border: 1px solid rgba(239,68,68,0.2); }
    .badge-admin   { background: rgba(245,158,11,0.12);  color: #fbbf24; border: 1px solid rgba(245,158,11,0.2); }
    .badge-bidder  { background: rgba(99,102,241,0.12);  color: #818cf8; border: 1px solid rgba(99,102,241,0.2); }
    .badge-enabled { background: rgba(16,185,129,0.12);  color: #34d399; border: 1px solid rgba(16,185,129,0.2); }
    .badge-disabled{ background: rgba(239,68,68,0.12);   color: #f87171; border: 1px solid rgba(239,68,68,0.2); }
    .badge-winning { background: rgba(245,158,11,0.12);  color: #fbbf24; border: 1px solid rgba(245,158,11,0.2); }

    /* Action buttons */
    .btn-action {
      padding: 4px 10px;
      border-radius: 7px;
      border: 1px solid;
      font-size: 0.73rem;
      font-weight: 600;
      font-family: 'Inter', sans-serif;
      cursor: pointer;
      transition: all 0.18s;
      display: inline-flex;
      align-items: center;
      gap: 0.25rem;
      text-decoration: none;
      white-space: nowrap;
    }

    .btn-danger {
      background: rgba(239,68,68,0.1);
      border-color: rgba(239,68,68,0.25);
      color: #f87171;
    }
    .btn-danger:hover {
      background: rgba(239,68,68,0.2);
      border-color: rgba(239,68,68,0.5);
      transform: scale(1.03);
    }

    .btn-warn {
      background: rgba(245,158,11,0.1);
      border-color: rgba(245,158,11,0.25);
      color: #fbbf24;
    }
    .btn-warn:hover {
      background: rgba(245,158,11,0.2);
      border-color: rgba(245,158,11,0.45);
    }

    .btn-info {
      background: rgba(59,130,246,0.1);
      border-color: rgba(59,130,246,0.25);
      color: #93c5fd;
    }
    .btn-info:hover {
      background: rgba(59,130,246,0.2);
      border-color: rgba(59,130,246,0.45);
    }

    .btn-success {
      background: rgba(16,185,129,0.1);
      border-color: rgba(16,185,129,0.25);
      color: #6ee7b7;
    }
    .btn-success:hover {
      background: rgba(16,185,129,0.2);
      border-color: rgba(16,185,129,0.45);
    }

    .btn-ghost {
      background: rgba(255,255,255,0.04);
      border-color: var(--border);
      color: var(--muted);
    }
    .btn-ghost:hover {
      background: rgba(255,255,255,0.07);
      color: var(--text);
    }

    /* Item title link */
    .item-link {
      color: var(--accent2);
      text-decoration: none;
      font-weight: 500;
    }
    .item-link:hover { text-decoration: underline; }

    /* Amount */
    .amount { font-weight: 700; color: var(--gold); font-variant-numeric: tabular-nums; }

    /* Empty state */
    .empty-state {
      text-align: center;
      padding: 3rem;
      color: var(--muted);
    }
    .empty-state .empty-icon { font-size: 2.5rem; margin-bottom: 0.75rem; }
    .empty-state p { font-size: 0.84rem; }

    /* Actions cell */
    .actions-cell {
      display: flex;
      align-items: center;
      gap: 0.4rem;
      flex-wrap: wrap;
    }

    /* Responsive */
    @media (max-width: 1200px) {
      .stats-grid { grid-template-columns: repeat(2,1fr); }
    }

    @media (max-width: 768px) {
      .sidebar { transform: translateX(-100%); }
      .main { margin-left: 0; }
      .stats-grid { grid-template-columns: 1fr; }
    }

    /* Scroll inside table */
    .table-scroll { overflow-x: auto; }

    /* Confirm delete modal */
    .modal-overlay {
      display: none;
      position: fixed;
      inset: 0;
      background: rgba(0,0,0,0.6);
      z-index: 999;
      align-items: center;
      justify-content: center;
      backdrop-filter: blur(4px);
    }

    .modal-overlay.open { display: flex; }

    .modal-box {
      background: var(--card);
      border: 1px solid rgba(239,68,68,0.3);
      border-radius: 16px;
      padding: 1.75rem;
      max-width: 380px;
      width: 90%;
      box-shadow: 0 25px 60px rgba(0,0,0,0.5);
      animation: modalIn 0.2s ease;
    }

    @keyframes modalIn {
      from { opacity:0; transform: scale(0.9); }
      to   { opacity:1; transform: scale(1); }
    }

    .modal-box h3 {
      font-size: 1.05rem;
      font-weight: 700;
      color: var(--text);
      margin-bottom: 0.5rem;
      display: flex;
      align-items: center;
      gap: 0.5rem;
    }

    .modal-box p { font-size: 0.83rem; color: var(--muted); margin-bottom: 1.25rem; }

    .modal-btns { display: flex; gap: 0.65rem; }

    .btn-modal-cancel {
      flex: 1;
      padding: 0.65rem;
      background: rgba(255,255,255,0.05);
      border: 1px solid var(--border);
      border-radius: 9px;
      color: var(--muted);
      font-family: 'Inter', sans-serif;
      font-size: 0.83rem;
      font-weight: 600;
      cursor: pointer;
      transition: all 0.18s;
    }

    .btn-modal-cancel:hover { background: rgba(255,255,255,0.09); color: var(--text); }

    .btn-modal-confirm {
      flex: 1;
      padding: 0.65rem;
      background: linear-gradient(135deg, #ef4444, #f87171);
      border: none;
      border-radius: 9px;
      color: #fff;
      font-family: 'Inter', sans-serif;
      font-size: 0.83rem;
      font-weight: 700;
      cursor: pointer;
      transition: all 0.18s;
    }

    .btn-modal-confirm:hover { opacity: 0.9; transform: translateY(-1px); }

    /* Hidden confirm forms */
    .hidden-form { display: none; }

    /* ─── ANALYTICS / CHARTS ─────────────────────────────── */
    .charts-row {
      display: grid;
      grid-template-columns: 1.6fr 1fr;
      gap: 1.25rem;
      margin-bottom: 1.5rem;
    }

    .chart-card {
      background: var(--card);
      border: 1px solid var(--border);
      border-radius: 14px;
      padding: 1.4rem;
      position: relative;
    }

    .chart-card-title {
      font-size: 0.88rem;
      font-weight: 700;
      color: var(--text);
      margin-bottom: 0.25rem;
      display: flex;
      align-items: center;
      gap: 0.45rem;
    }

    .chart-card-sub {
      font-size: 0.72rem;
      color: var(--muted2);
      margin-bottom: 1.1rem;
    }

    .chart-canvas-wrap {
      position: relative;
      width: 100%;
    }

    .analytics-summary {
      display: grid;
      grid-template-columns: repeat(3, 1fr);
      gap: 0.85rem;
      margin-bottom: 1.25rem;
    }

    .summary-chip {
      background: var(--card2);
      border: 1px solid var(--border);
      border-radius: 11px;
      padding: 0.9rem 1rem;
      text-align: center;
      transition: transform 0.18s;
    }
    .summary-chip:hover { transform: translateY(-2px); }
    .summary-chip .chip-val {
      font-size: 1.55rem;
      font-weight: 800;
      line-height: 1;
      margin-bottom: 0.25rem;
    }
    .summary-chip .chip-label {
      font-size: 0.69rem;
      text-transform: uppercase;
      letter-spacing: 0.7px;
      color: var(--muted2);
      font-weight: 600;
    }
    .chip-indigo .chip-val { color: var(--accent2); }
    .chip-green  .chip-val { color: #34d399; }
    .chip-gold   .chip-val { color: #fbbf24; }
  </style>
</head>
<body>

<!-- ─── DELETE CONFIRM MODAL ─────────────────────────────────── -->
<div class="modal-overlay" id="deleteModal">
  <div class="modal-box">
    <h3>🗑️ Confirm Delete</h3>
    <p id="modalMsg">Are you sure you want to permanently delete this? This action cannot be undone.</p>
    <div class="modal-btns">
      <button class="btn-modal-cancel" onclick="closeModal()">Cancel</button>
      <button class="btn-modal-confirm" id="modalConfirmBtn" onclick="submitDeleteForm()">Delete</button>
    </div>
  </div>
</div>

<!-- ─── SIDEBAR ─────────────────────────────────────────────── -->
<aside class="sidebar">
  <div class="sidebar-brand">
    <div class="brand-logo">
      <div class="ico">🏛️</div>
      <div>
        <div class="name">AuctionHub</div>
        <div class="sub">Admin Panel</div>
      </div>
    </div>
  </div>

  <div class="admin-info">
    <div class="admin-avatar">A</div>
    <div>
      <div class="aname">${sessionScope.adminUsername}</div>
      <div class="arole">ADMINISTRATOR</div>
    </div>
  </div>

  <div class="nav-section">
    <div class="nav-label">Overview</div>
    <button class="nav-item" id="nb-analytics" onclick="showTab('analytics','nb-analytics','tb-analytics','📊 Analytics','Data Insights')">
      <span>📊</span> Analytics
    </button>
    <button class="nav-item active" id="nb-bids" onclick="showTab('bids','nb-bids','tb-bids','💸 All Bids','Bid Management')">
      <span>💸</span> All Bids
      <span class="badge-count">${allBids.size()}</span>
    </button>
    <button class="nav-item" id="nb-auctions" onclick="showTab('auctions','nb-auctions','tb-auctions','🔨 Auctions','Auction Management')">
      <span>🔨</span> Auctions
      <span class="badge-count">${allItems.size()}</span>
    </button>
    <button class="nav-item" id="nb-users" onclick="showTab('users','nb-users','tb-users','👥 Users','User Management')">
      <span>👥</span> Users
      <span class="badge-count">${allUsers.size()}</span>
    </button>
    <button class="nav-item" id="nb-winners" onclick="showTab('winners','nb-winners','tb-winners','🏆 Winners','Payment Management')">
      <span>🏆</span> Winners
      <span class="badge-count">${allWinners.size()}</span>
    </button>
  </div>

  <div class="sidebar-footer">
    <a href="${pageContext.request.contextPath}/LogoutServlet" class="btn-logout">
      <span>🚪</span> Logout
    </a>
  </div>
</aside>

<!-- ─── MAIN ─────────────────────────────────────────────────── -->
<main class="main">

  <!-- Topbar -->
  <div class="topbar">
    <div>
      <h2 id="topbar-title">💸 All Bids</h2>
      <div class="breadcrumb">AuctionHub › Admin › <span id="topbar-sub">Bid Management</span></div>
    </div>
    <div class="topbar-right">
      <div class="status-dot"></div>
      <span class="status-label">System Online</span>
    </div>
  </div>

  <!-- Content -->
  <div class="content">

    <!-- Toast Messages -->
    <c:if test="${not empty msg}">
      <c:choose>
        <c:when test="${msg == 'bid_deleted'}">
          <div class="toast success">✅ Bid successfully deleted.</div>
        </c:when>
        <c:when test="${msg == 'item_deleted'}">
          <div class="toast success">✅ Auction item and all associated bids deleted.</div>
        </c:when>
        <c:when test="${msg == 'auction_closed'}">
          <div class="toast success">✅ Auction closed and winner declared.</div>
        </c:when>
        <c:when test="${msg == 'user_updated'}">
          <div class="toast success">✅ User status updated.</div>
        </c:when>
        <c:when test="${msg == 'payment_updated'}">
          <div class="toast success">✅ Payment status updated.</div>
        </c:when>
        <c:otherwise>
          <div class="toast error">⚠️ An error occurred. Please try again.</div>
        </c:otherwise>
      </c:choose>
    </c:if>

    <!-- Stats Grid -->
    <div class="stats-grid">
      <div class="stat-card indigo">
        <div class="stat-label">Total Bids</div>
        <div class="stat-value">${allBids.size()}</div>
        <div class="stat-sub">Across all auctions</div>
        <div class="stat-icon">💸</div>
      </div>
      <div class="stat-card green">
        <div class="stat-label">Active Auctions</div>
        <div class="stat-value">${activeAuctionCount}</div>
        <div class="stat-sub">Currently running</div>
        <div class="stat-icon">🔨</div>
      </div>
      <div class="stat-card gold">
        <div class="stat-label">Closed Auctions</div>
        <div class="stat-value">${closedAuctionCount}</div>
        <div class="stat-sub">Completed auctions</div>
        <div class="stat-icon">🏁</div>
      </div>
      <div class="stat-card red">
        <div class="stat-label">Total Users</div>
        <div class="stat-value">${allUsers.size()}</div>
        <div class="stat-sub">Registered accounts</div>
        <div class="stat-icon">👥</div>
      </div>
    </div>

    <!-- Tab Bar -->
    <div class="tab-bar">
      <button class="tab-btn" id="tb-analytics" onclick="showTab('analytics','nb-analytics','tb-analytics','📊 Analytics','Data Insights')">
        📊 Analytics
      </button>
      <button class="tab-btn active" id="tb-bids" onclick="showTab('bids','nb-bids','tb-bids','💸 All Bids','Bid Management')">
        💸 All Bids <span class="tab-pill">${allBids.size()}</span>
      </button>
      <button class="tab-btn" id="tb-auctions" onclick="showTab('auctions','nb-auctions','tb-auctions','🔨 Auctions','Auction Management')">
        🔨 Auctions <span class="tab-pill">${allItems.size()}</span>
      </button>
      <button class="tab-btn" id="tb-users" onclick="showTab('users','nb-users','tb-users','👥 Users','User Management')">
        👥 Users <span class="tab-pill">${allUsers.size()}</span>
      </button>
      <button class="tab-btn" id="tb-winners" onclick="showTab('winners','nb-winners','tb-winners','🏆 Winners','Payment Management')">
        🏆 Winners <span class="tab-pill">${allWinners.size()}</span>
      </button>
    </div>

    <%-- ══════════════════════════════════════════════════════
         TAB 0: ANALYTICS
         ══════════════════════════════════════════════════════ --%>
    <div id="tab-analytics" class="tab-content">

      <!-- Summary Chips -->
      <div class="analytics-summary">
        <div class="summary-chip chip-indigo">
          <div class="chip-val">${allBids.size()}</div>
          <div class="chip-label">Total Bids</div>
        </div>
        <div class="summary-chip chip-green">
          <div class="chip-val">${activeAuctionCount}</div>
          <div class="chip-label">Active Auctions</div>
        </div>
        <div class="summary-chip chip-gold">
          <div class="chip-val">${allUsers.size()}</div>
          <div class="chip-label">Registered Users</div>
        </div>
      </div>

      <!-- Charts Row -->
      <div class="charts-row">

        <!-- Bar Chart: Bids per Auction Item -->
        <div class="chart-card">
          <div class="chart-card-title">📈 Bids per Auction Item</div>
          <div class="chart-card-sub">Top items by bid count</div>
          <div class="chart-canvas-wrap" style="height:280px;">
            <canvas id="barChart"></canvas>
          </div>
        </div>

        <!-- Pie Chart: Auction Status Distribution -->
        <div class="chart-card">
          <div class="chart-card-title">🥧 Auction Status</div>
          <div class="chart-card-sub">Active vs Closed breakdown</div>
          <div class="chart-canvas-wrap" style="height:280px;">
            <canvas id="pieChart"></canvas>
          </div>
        </div>

      </div>

      <!-- Second Row: Users & Payment Pie -->
      <div class="charts-row">

        <!-- Donut: Payment Status -->
        <div class="chart-card">
          <div class="chart-card-title">💳 Payment Status Distribution</div>
          <div class="chart-card-sub">Winner payment outcomes</div>
          <div class="chart-canvas-wrap" style="height:260px;">
            <canvas id="paymentChart"></canvas>
          </div>
        </div>

        <!-- Bar: Bid amount spread across auctions -->
        <div class="chart-card">
          <div class="chart-card-title">💰 Starting vs Current Price</div>
          <div class="chart-card-sub">Price growth per auction item</div>
          <div class="chart-canvas-wrap" style="height:260px;">
            <canvas id="priceChart"></canvas>
          </div>
        </div>

      </div>

    </div><!-- /tab-analytics -->

    <%-- ══════════════════════════════════════════════════════
         TAB 1: ALL BIDS
         ══════════════════════════════════════════════════════ --%>
    <div id="tab-bids" class="tab-content active-tab">
      <div class="section-header">
        <h3 class="section-title">All Bids</h3>
        <div class="search-box">
          <span class="search-ico">🔍</span>
          <input type="text" id="bidSearch" placeholder="Search bidder/item..." onkeyup="filterTable('bidTable','bidSearch')">
        </div>
      </div>

      <div class="table-container">
        <div class="table-scroll">
          <table id="bidTable">
            <thead>
              <tr>
                <th>#Bid ID</th>
                <th>Auction Item</th>
                <th>Bidder</th>
                <th>Amount (₹)</th>
                <th>Bid Time</th>
                <th>Status</th>
                <th>Action</th>
              </tr>
            </thead>
            <tbody>
              <c:choose>
                <c:when test="${empty allBids}">
                  <tr><td colspan="7">
                    <div class="empty-state">
                      <div class="empty-icon">💸</div>
                      <p>No bids placed yet.</p>
                    </div>
                  </td></tr>
                </c:when>
                <c:otherwise>
                  <c:forEach var="bid" items="${allBids}">
                    <tr>
                      <td><code style="color:var(--muted2);font-size:0.76rem;">#${bid.bidId}</code></td>
                      <td>
                        <a class="item-link" href="${pageContext.request.contextPath}/BidServlet?itemId=${bid.itemId}"
                           target="_blank">${bid.itemTitle}</a>
                        <div style="font-size:0.7rem;color:var(--muted2);">Item #${bid.itemId}</div>
                      </td>
                      <td>
                        <div style="font-weight:600;">${bid.bidderName}</div>
                        <div style="font-size:0.7rem;color:var(--muted2);">ID: ${bid.bidderId}</div>
                      </td>
                      <td><span class="amount">₹<fmt:formatNumber value="${bid.bidAmount}" pattern="#,##0.00"/></span></td>
                      <td><fmt:formatDate value="${bid.bidTime}" pattern="dd MMM yyyy HH:mm"/></td>
                      <td>
                        <c:choose>
                          <c:when test="${bid.winning}">
                            <span class="badge badge-winning">🥇 Winning</span>
                          </c:when>
                          <c:otherwise>
                            <span class="badge badge-bidder">Outbid</span>
                          </c:otherwise>
                        </c:choose>
                      </td>
                      <td>
                        <button class="btn-action btn-danger"
                                data-type="bid"
                                data-id="${bid.bidId}"
                                data-msg="Delete Bid #${bid.bidId} by ${bid.bidderName}?"
                                onclick="confirmDeleteBtn(this)">
                          🗑️ Delete
                        </button>
                        <%-- Hidden delete form for this bid --%>
                        <form id="del-bid-${bid.bidId}" class="hidden-form"
                              method="post" action="${pageContext.request.contextPath}/AdminServlet">
                          <input type="hidden" name="action"    value="deleteBid">
                          <input type="hidden" name="bidId"     value="${bid.bidId}">
                          <input type="hidden" name="csrfToken" value="${csrfToken}">
                        </form>
                      </td>
                    </tr>
                  </c:forEach>
                </c:otherwise>
              </c:choose>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <%-- ══════════════════════════════════════════════════════
         TAB 2: AUCTIONS
         ══════════════════════════════════════════════════════ --%>
    <div id="tab-auctions" class="tab-content">
      <div class="section-header">
        <h3 class="section-title">All Auctions</h3>
        <div class="search-box">
          <span class="search-ico">🔍</span>
          <input type="text" id="auctionSearch" placeholder="Search title/category..." onkeyup="filterTable('auctionTable','auctionSearch')">
        </div>
      </div>

      <div class="table-container">
        <div class="table-scroll">
          <table id="auctionTable">
            <thead>
              <tr>
                <th>#ID</th>
                <th>Title</th>
                <th>Category</th>
                <th>Starting (₹)</th>
                <th>Current (₹)</th>
                <th>Status</th>
                <th>End Time</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              <c:choose>
                <c:when test="${empty allItems}">
                  <tr><td colspan="8">
                    <div class="empty-state">
                      <div class="empty-icon">🔨</div>
                      <p>No auction items found.</p>
                    </div>
                  </td></tr>
                </c:when>
                <c:otherwise>
                  <c:forEach var="item" items="${allItems}">
                    <tr>
                      <td><code style="color:var(--muted2);font-size:0.76rem;">#${item.itemId}</code></td>
                      <td>
                        <a class="item-link" href="${pageContext.request.contextPath}/BidServlet?itemId=${item.itemId}"
                           target="_blank">${item.title}</a>
                        <div style="font-size:0.7rem;color:var(--muted2);">By: ${item.sellerName}</div>
                      </td>
                      <td><span style="color:var(--muted);font-size:0.8rem;">${item.category}</span></td>
                      <td><span class="amount">₹<fmt:formatNumber value="${item.startingPrice}" pattern="#,##0.00"/></span></td>
                      <td><span class="amount">₹<fmt:formatNumber value="${item.currentPrice}"  pattern="#,##0.00"/></span></td>
                      <td>
                        <c:choose>
                          <c:when test="${item.status == 'ACTIVE'}">
                            <span class="badge badge-active">🟢 Active</span>
                          </c:when>
                          <c:otherwise>
                            <span class="badge badge-closed">🔵 Closed</span>
                          </c:otherwise>
                        </c:choose>
                      </td>
                      <td style="font-size:0.78rem;color:var(--muted);">
                        <fmt:formatDate value="${item.endTime}" pattern="dd MMM yyyy HH:mm"/>
                      </td>
                      <td>
                        <div class="actions-cell">
                          <!-- Close auction (only if ACTIVE) -->
                          <c:if test="${item.status == 'ACTIVE'}">
                            <form method="post" action="${pageContext.request.contextPath}/AdminServlet" style="display:inline;">
                              <input type="hidden" name="action"    value="closeAuction">
                              <input type="hidden" name="itemId"    value="${item.itemId}">
                              <input type="hidden" name="csrfToken" value="${csrfToken}">
                              <button type="submit" class="btn-action btn-warn"
                                      onclick="return confirm('Close auction and declare winner?')">
                                🏁 Close
                              </button>
                            </form>
                          </c:if>

                          <!-- CSV Export -->
                          <a class="btn-action btn-ghost"
                             href="${pageContext.request.contextPath}/AdminServlet?action=exportBidsCsv&amp;itemId=${item.itemId}"
                             title="Download bid CSV">📥 CSV</a>

                          <!-- Delete Item -->
                          <button class="btn-action btn-danger"
                                  data-type="item"
                                  data-id="${item.itemId}"
                                  data-msg="Delete auction '${item.title}' and ALL its bids permanently?"
                                  onclick="confirmDeleteBtn(this)">
                            🗑️ Delete
                          </button>
                          <form id="del-item-${item.itemId}" class="hidden-form"
                                method="post" action="${pageContext.request.contextPath}/AdminServlet">
                            <input type="hidden" name="action"    value="deleteItem">
                            <input type="hidden" name="itemId"    value="${item.itemId}">
                            <input type="hidden" name="csrfToken" value="${csrfToken}">
                          </form>
                        </div>
                      </td>
                    </tr>
                  </c:forEach>
                </c:otherwise>
              </c:choose>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <%-- ══════════════════════════════════════════════════════
         TAB 3: USERS
         ══════════════════════════════════════════════════════ --%>
    <div id="tab-users" class="tab-content">
      <div class="section-header">
        <h3 class="section-title">All Users</h3>
        <div class="search-box">
          <span class="search-ico">🔍</span>
          <input type="text" id="userSearch" placeholder="Search username/email..." onkeyup="filterTable('userTable','userSearch')">
        </div>
      </div>

      <div class="table-container">
        <div class="table-scroll">
          <table id="userTable">
            <thead>
              <tr>
                <th>#ID</th>
                <th>Username</th>
                <th>Email</th>
                <th>Full Name</th>
                <th>Role</th>
                <th>Joined</th>
                <th>Status</th>
                <th>Action</th>
              </tr>
            </thead>
            <tbody>
              <c:choose>
                <c:when test="${empty allUsers}">
                  <tr><td colspan="8">
                    <div class="empty-state">
                      <div class="empty-icon">👥</div>
                      <p>No users found.</p>
                    </div>
                  </td></tr>
                </c:when>
                <c:otherwise>
                  <c:forEach var="u" items="${allUsers}">
                    <tr>
                      <td><code style="color:var(--muted2);font-size:0.76rem;">#${u.userId}</code></td>
                      <td><strong>${u.username}</strong></td>
                      <td style="color:var(--muted);font-size:0.8rem;">${u.email}</td>
                      <td>${u.fullName}</td>
                      <td>
                        <c:choose>
                          <c:when test="${u.role == 'ADMIN'}">
                            <span class="badge badge-admin">👑 Admin</span>
                          </c:when>
                          <c:otherwise>
                            <span class="badge badge-bidder">Bidder</span>
                          </c:otherwise>
                        </c:choose>
                      </td>
                      <td style="font-size:0.78rem;color:var(--muted);">
                        <fmt:formatDate value="${u.createdAt}" pattern="dd MMM yyyy"/>
                      </td>
                      <td>
                        <c:choose>
                          <c:when test="${u.active}">
                            <span class="badge badge-enabled">● Active</span>
                          </c:when>
                          <c:otherwise>
                            <span class="badge badge-disabled">○ Disabled</span>
                          </c:otherwise>
                        </c:choose>
                      </td>
                      <td>
                        <form method="post" action="${pageContext.request.contextPath}/AdminServlet" style="display:inline;">
                          <input type="hidden" name="action"    value="toggleUser">
                          <input type="hidden" name="userId"    value="${u.userId}">
                          <input type="hidden" name="active"    value="${!u.active}">
                          <input type="hidden" name="csrfToken" value="${csrfToken}">
                          <button type="submit"
                                  class="btn-action ${u.active ? 'btn-warn' : 'btn-success'}"
                                  onclick="return confirm(this.dataset.msg)"
                                  data-msg="${u.active ? 'Disable' : 'Enable'} user ${u.username}?">
                            ${u.active ? '🚫 Disable' : '✅ Enable'}
                          </button>
                        </form>
                      </td>
                    </tr>
                  </c:forEach>
                </c:otherwise>
              </c:choose>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <%-- ══════════════════════════════════════════════════════
         TAB 4: WINNERS
         ══════════════════════════════════════════════════════ --%>
    <div id="tab-winners" class="tab-content">
      <div class="section-header">
        <h3 class="section-title">Auction Winners &amp; Payments</h3>
        <div class="search-box">
          <span class="search-ico">🔍</span>
          <input type="text" id="winnerSearch" placeholder="Search winner/item..." onkeyup="filterTable('winnerTable','winnerSearch')">
        </div>
      </div>

      <div class="table-container">
        <div class="table-scroll">
          <table id="winnerTable">
            <thead>
              <tr>
                <th>#ID</th>
                <th>Auction Item</th>
                <th>Winner</th>
                <th>Winning Amount (₹)</th>
                <th>Payment</th>
                <th>Awarded At</th>
                <th>Action</th>
              </tr>
            </thead>
            <tbody>
              <c:choose>
                <c:when test="${empty allWinners}">
                  <tr><td colspan="7">
                    <div class="empty-state">
                      <div class="empty-icon">🏆</div>
                      <p>No auction winners yet.</p>
                    </div>
                  </td></tr>
                </c:when>
                <c:otherwise>
                  <c:forEach var="w" items="${allWinners}">
                    <tr>
                      <td><code style="color:var(--muted2);font-size:0.76rem;">#${w.winnerId}</code></td>
                      <td>
                        <span style="font-weight:600;">${w.itemTitle}</span>
                        <div style="font-size:0.7rem;color:var(--muted2);">Item #${w.itemId}</div>
                      </td>
                      <td>
                        <div style="font-weight:600;">🏆 ${w.winnerName}</div>
                        <div style="font-size:0.7rem;color:var(--muted2);">${w.winnerEmail}</div>
                      </td>
                      <td><span class="amount">₹<fmt:formatNumber value="${w.winningAmount}" pattern="#,##0.00"/></span></td>
                      <td>
                        <c:choose>
                          <c:when test="${w.paymentStatus == 'PAID'}">
                            <span class="badge badge-paid">✅ Paid</span>
                          </c:when>
                          <c:when test="${w.paymentStatus == 'FAILED'}">
                            <span class="badge badge-failed">❌ Failed</span>
                          </c:when>
                          <c:otherwise>
                            <span class="badge badge-pending">⏳ Pending</span>
                          </c:otherwise>
                        </c:choose>
                      </td>
                      <td style="font-size:0.78rem;color:var(--muted);">
                        <fmt:formatDate value="${w.awardedAt}" pattern="dd MMM yyyy HH:mm"/>
                      </td>
                      <td>
                        <c:if test="${w.paymentStatus != 'PAID'}">
                          <form method="post" action="${pageContext.request.contextPath}/AdminServlet" style="display:inline;">
                            <input type="hidden" name="action"    value="updatePayment">
                            <input type="hidden" name="winnerId"  value="${w.winnerId}">
                            <input type="hidden" name="status"    value="PAID">
                            <input type="hidden" name="csrfToken" value="${csrfToken}">
                            <button type="submit" class="btn-action btn-success">
                              ✅ Mark Paid
                            </button>
                          </form>
                        </c:if>
                        <c:if test="${w.paymentStatus == 'PAID'}">
                          <span style="color:var(--muted2);font-size:0.76rem;">Payment done</span>
                        </c:if>
                      </td>
                    </tr>
                  </c:forEach>
                </c:otherwise>
              </c:choose>
            </tbody>
          </table>
        </div>
      </div>
    </div>

  </div><!-- /content -->
</main>

<script>
// ── Inject server data as JS variables ───────────────────────────
const serverData = {
  items: [
    <c:forEach var="item" items="${allItems}" varStatus="vs">
      { title: "${item.title}", bidCount: ${item.bidCount != null ? item.bidCount : 0}, status: "${item.status}", starting: ${item.startingPrice}, current: ${item.currentPrice} }<c:if test="${!vs.last}">,</c:if>
    </c:forEach>
  ],
  winners: [
    <c:forEach var="w" items="${allWinners}" varStatus="vs">
      { status: "${w.paymentStatus}" }<c:if test="${!vs.last}">,</c:if>
    </c:forEach>
  ],
  totals: {
    bids: ${allBids.size()},
    active: ${activeAuctionCount},
    closed: ${closedAuctionCount},
    users: ${allUsers.size()},
    winners: ${allWinners.size()}
  }
};

// ── Tab switcher ─────────────────────────────────────────────────
let pendingFormId = null;
let chartsInitialized = false;

function showTab(name, navId, tabBtnId, title, sub) {
  // Hide all tab contents
  document.querySelectorAll('.tab-content').forEach(t => t.classList.remove('active-tab'));
  // Deactivate all nav items and tab buttons
  document.querySelectorAll('.nav-item').forEach(n => n.classList.remove('active'));
  document.querySelectorAll('.tab-btn').forEach(b => b.classList.remove('active'));

  // Show selected tab
  document.getElementById('tab-' + name).classList.add('active-tab');

  // Activate nav item
  const nb = document.getElementById(navId);
  if (nb) nb.classList.add('active');

  // Activate tab button (optional 3rd param from tab bar buttons)
  if (tabBtnId) {
    const tb = document.getElementById(tabBtnId);
    if (tb) tb.classList.add('active');
  }

  // Update topbar
  if (title) document.getElementById('topbar-title').textContent = title;
  if (sub)   document.getElementById('topbar-sub').textContent   = sub;

  // Lazy-initialize charts when analytics tab is first shown
  if (name === 'analytics') initCharts();
}

// ── Live table search filter ──────────────────────────────────────
function filterTable(tableId, inputId) {
  const val = document.getElementById(inputId).value.toLowerCase();
  const rows = document.querySelectorAll('#' + tableId + ' tbody tr');
  rows.forEach(row => {
    row.style.display = row.textContent.toLowerCase().includes(val) ? '' : 'none';
  });
}

// ── Delete modal ──────────────────────────────────────────────────
function confirmDelete(type, id, message) {
  pendingFormId = 'del-' + type + '-' + id;
  document.getElementById('modalMsg').textContent = message;
  document.getElementById('deleteModal').classList.add('open');
}

// Triggered from data-* attribute buttons (avoids inline quote nesting)
function confirmDeleteBtn(btn) {
  const type = btn.dataset.type;
  const id   = btn.dataset.id;
  const msg  = btn.dataset.msg;
  confirmDelete(type, id, msg);
}

function closeModal() {
  document.getElementById('deleteModal').classList.remove('open');
  pendingFormId = null;
}

function submitDeleteForm() {
  if (pendingFormId) {
    const form = document.getElementById(pendingFormId);
    if (form) form.submit();
  }
  closeModal();
}

// Close modal on overlay click
document.getElementById('deleteModal').addEventListener('click', function(e) {
  if (e.target === this) closeModal();
});

// Auto-dismiss toast after 4 seconds
setTimeout(() => {
  const toasts = document.querySelectorAll('.toast');
  toasts.forEach(t => {
    t.style.transition = 'opacity 0.5s';
    t.style.opacity = '0';
    setTimeout(() => t.remove(), 500);
  });
}, 4000);

// ── Chart.js initialization ───────────────────────────────────────
function initCharts() {
  if (chartsInitialized) return;
  chartsInitialized = true;

  Chart.defaults.color = '#94a3b8';
  Chart.defaults.borderColor = 'rgba(99,102,241,0.12)';
  Chart.defaults.font.family = "'Inter', sans-serif";
  Chart.defaults.font.size = 11;

  const ACCENT   = '#818cf8';
  const GOLD     = '#f59e0b';
  const GREEN    = '#10b981';
  const DANGER   = '#ef4444';
  const MUTED    = '#64748b';

  // ── Bar Chart: Bids per Item ────────────────────────────────
  const topItems = serverData.items
    .filter(i => i.bidCount > 0)
    .sort((a,b) => b.bidCount - a.bidCount)
    .slice(0, 8);

  const barLabels  = topItems.map(i => i.title.length > 22 ? i.title.slice(0,22)+'…' : i.title);
  const barData    = topItems.map(i => i.bidCount);

  new Chart(document.getElementById('barChart'), {
    type: 'bar',
    data: {
      labels: barLabels,
      datasets: [{
        label: 'Bids',
        data: barData,
        backgroundColor: barData.map((_,i) =>
          `hsla(${238 + i*6},80%,70%,0.75)`
        ),
        borderColor: barData.map((_,i) =>
          `hsla(${238 + i*6},80%,70%,1)`
        ),
        borderWidth: 1.5,
        borderRadius: 6,
        borderSkipped: false,
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: { display: false },
        tooltip: {
          backgroundColor: '#111827',
          borderColor: 'rgba(99,102,241,0.3)',
          borderWidth: 1,
          callbacks: {
            label: ctx => ` ${ctx.parsed.y} bids`
          }
        }
      },
      scales: {
        x: {
          grid: { color: 'rgba(99,102,241,0.06)' },
          ticks: { maxRotation: 35, minRotation: 20 }
        },
        y: {
          grid: { color: 'rgba(99,102,241,0.08)' },
          beginAtZero: true,
          ticks: { stepSize: 1 }
        }
      }
    }
  });

  // ── Pie Chart: Active vs Closed ─────────────────────────────
  new Chart(document.getElementById('pieChart'), {
    type: 'doughnut',
    data: {
      labels: ['Active', 'Closed'],
      datasets: [{
        data: [serverData.totals.active, serverData.totals.closed],
        backgroundColor: [
          'rgba(16,185,129,0.75)',
          'rgba(99,102,241,0.75)'
        ],
        borderColor: ['#10b981','#6366f1'],
        borderWidth: 2,
        hoverOffset: 8
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      cutout: '62%',
      plugins: {
        legend: {
          position: 'bottom',
          labels: {
            padding: 16,
            usePointStyle: true,
            pointStyleWidth: 10
          }
        },
        tooltip: {
          backgroundColor: '#111827',
          borderColor: 'rgba(99,102,241,0.3)',
          borderWidth: 1
        }
      }
    }
  });

  // ── Donut: Payment Status ───────────────────────────────────
  const paidCount    = serverData.winners.filter(w => w.status === 'PAID').length;
  const pendingCount = serverData.winners.filter(w => w.status === 'PENDING').length;
  const failedCount  = serverData.winners.filter(w => w.status === 'FAILED').length;

  new Chart(document.getElementById('paymentChart'), {
    type: 'doughnut',
    data: {
      labels: ['Paid', 'Pending', 'Failed'],
      datasets: [{
        data: [paidCount, pendingCount, failedCount],
        backgroundColor: [
          'rgba(16,185,129,0.75)',
          'rgba(245,158,11,0.75)',
          'rgba(239,68,68,0.75)'
        ],
        borderColor: ['#10b981','#f59e0b','#ef4444'],
        borderWidth: 2,
        hoverOffset: 8
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      cutout: '60%',
      plugins: {
        legend: {
          position: 'bottom',
          labels: { padding: 14, usePointStyle: true, pointStyleWidth: 10 }
        },
        tooltip: {
          backgroundColor: '#111827',
          borderColor: 'rgba(99,102,241,0.3)',
          borderWidth: 1
        }
      }
    }
  });

  // ── Grouped Bar: Starting vs Current Price ──────────────────
  const priceItems = serverData.items
    .sort((a,b) => b.current - a.current)
    .slice(0, 7);
  const priceLabels = priceItems.map(i => i.title.length > 18 ? i.title.slice(0,18)+'…' : i.title);

  new Chart(document.getElementById('priceChart'), {
    type: 'bar',
    data: {
      labels: priceLabels,
      datasets: [
        {
          label: 'Starting ₹',
          data: priceItems.map(i => i.starting),
          backgroundColor: 'rgba(99,102,241,0.55)',
          borderColor: '#6366f1',
          borderWidth: 1.5,
          borderRadius: 5,
          borderSkipped: false
        },
        {
          label: 'Current ₹',
          data: priceItems.map(i => i.current),
          backgroundColor: 'rgba(245,158,11,0.65)',
          borderColor: '#f59e0b',
          borderWidth: 1.5,
          borderRadius: 5,
          borderSkipped: false
        }
      ]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: {
          position: 'bottom',
          labels: { padding: 12, usePointStyle: true, pointStyleWidth: 10 }
        },
        tooltip: {
          backgroundColor: '#111827',
          borderColor: 'rgba(99,102,241,0.3)',
          borderWidth: 1,
          callbacks: {
            label: ctx => ` ₹${ctx.parsed.y.toLocaleString('en-IN')}`
          }
        }
      },
      scales: {
        x: { grid: { color: 'rgba(99,102,241,0.06)' }, ticks: { maxRotation: 30 } },
        y: { grid: { color: 'rgba(99,102,241,0.08)' }, beginAtZero: true }
      }
    }
  });
}

</script>
</body>
</html>
