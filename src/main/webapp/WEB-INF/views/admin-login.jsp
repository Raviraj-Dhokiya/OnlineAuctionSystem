<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Admin Login — AuctionHub</title>
  <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700;800&display=swap" rel="stylesheet">
  <style>
    *, *::before, *::after { box-sizing: border-box; margin: 0; padding: 0; }

    :root {
      --bg-dark:    #0a0e1a;
      --bg-card:    #111827;
      --border:     rgba(99,102,241,0.25);
      --accent:     #6366f1;
      --accent-glow:#818cf8;
      --danger:     #ef4444;
      --text:       #f1f5f9;
      --muted:      #94a3b8;
      --gold:       #f59e0b;
    }

    body {
      font-family: 'Inter', sans-serif;
      background: var(--bg-dark);
      min-height: 100vh;
      display: flex;
      align-items: center;
      justify-content: center;
      overflow: hidden;
      position: relative;
    }

    /* Animated background */
    body::before {
      content: '';
      position: fixed;
      inset: 0;
      background:
        radial-gradient(ellipse 80% 60% at 20% 20%, rgba(99,102,241,0.15) 0%, transparent 60%),
        radial-gradient(ellipse 60% 50% at 80% 80%, rgba(245,158,11,0.08) 0%, transparent 60%);
      pointer-events: none;
    }

    /* Floating grid bg */
    body::after {
      content: '';
      position: fixed;
      inset: 0;
      background-image:
        linear-gradient(rgba(99,102,241,0.05) 1px, transparent 1px),
        linear-gradient(90deg, rgba(99,102,241,0.05) 1px, transparent 1px);
      background-size: 50px 50px;
      pointer-events: none;
    }

    .login-wrapper {
      position: relative;
      z-index: 10;
      width: 100%;
      max-width: 420px;
      padding: 1.5rem;
    }

    /* Admin badge */
    .admin-badge {
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 0.5rem;
      margin-bottom: 1.5rem;
      animation: fadeDown 0.5s ease;
    }

    .admin-badge .icon {
      width: 52px;
      height: 52px;
      background: linear-gradient(135deg, #6366f1, #818cf8);
      border-radius: 14px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 1.6rem;
      box-shadow: 0 0 30px rgba(99,102,241,0.5);
    }

    .admin-badge .label h1 {
      font-size: 1.5rem;
      font-weight: 800;
      color: var(--text);
      letter-spacing: -0.5px;
    }

    .admin-badge .label span {
      font-size: 0.75rem;
      color: var(--accent-glow);
      font-weight: 500;
      text-transform: uppercase;
      letter-spacing: 1px;
    }

    /* Card */
    .login-card {
      background: var(--bg-card);
      border: 1px solid var(--border);
      border-radius: 20px;
      padding: 2rem;
      box-shadow:
        0 0 0 1px rgba(99,102,241,0.1),
        0 25px 50px rgba(0,0,0,0.5),
        inset 0 1px 0 rgba(255,255,255,0.05);
      animation: fadeUp 0.5s ease 0.1s both;
    }

    .card-header {
      text-align: center;
      margin-bottom: 1.75rem;
    }

    .card-header h2 {
      font-size: 1.2rem;
      font-weight: 700;
      color: var(--text);
    }

    .card-header p {
      font-size: 0.82rem;
      color: var(--muted);
      margin-top: 0.3rem;
    }

    /* Divider */
    .divider {
      height: 1px;
      background: linear-gradient(90deg, transparent, var(--border), transparent);
      margin: 1.25rem 0;
    }

    /* Form groups */
    .form-group {
      margin-bottom: 1.1rem;
    }

    .form-group label {
      display: block;
      font-size: 0.78rem;
      font-weight: 600;
      color: var(--muted);
      text-transform: uppercase;
      letter-spacing: 0.8px;
      margin-bottom: 0.5rem;
    }

    .input-wrap {
      position: relative;
    }

    .input-wrap .ico {
      position: absolute;
      left: 0.85rem;
      top: 50%;
      transform: translateY(-50%);
      font-size: 1rem;
      pointer-events: none;
    }

    .form-group input {
      width: 100%;
      padding: 0.75rem 1rem 0.75rem 2.5rem;
      background: rgba(255,255,255,0.04);
      border: 1px solid rgba(99,102,241,0.2);
      border-radius: 10px;
      color: var(--text);
      font-size: 0.9rem;
      font-family: 'Inter', sans-serif;
      transition: all 0.2s;
      outline: none;
    }

    .form-group input:focus {
      border-color: var(--accent);
      background: rgba(99,102,241,0.06);
      box-shadow: 0 0 0 3px rgba(99,102,241,0.15);
    }

    .form-group input::placeholder { color: #475569; }

    /* Alert */
    .alert-error {
      background: rgba(239,68,68,0.1);
      border: 1px solid rgba(239,68,68,0.3);
      border-radius: 10px;
      padding: 0.75rem 1rem;
      color: #fca5a5;
      font-size: 0.84rem;
      margin-bottom: 1.1rem;
      display: flex;
      align-items: center;
      gap: 0.5rem;
    }

    /* Submit button */
    .btn-login {
      width: 100%;
      padding: 0.85rem;
      background: linear-gradient(135deg, #6366f1, #818cf8);
      border: none;
      border-radius: 10px;
      color: #fff;
      font-size: 0.95rem;
      font-weight: 700;
      font-family: 'Inter', sans-serif;
      cursor: pointer;
      transition: all 0.2s;
      position: relative;
      overflow: hidden;
      letter-spacing: 0.3px;
      margin-top: 0.5rem;
    }

    .btn-login::before {
      content: '';
      position: absolute;
      inset: 0;
      background: linear-gradient(135deg, rgba(255,255,255,0.15), transparent);
      opacity: 0;
      transition: opacity 0.2s;
    }

    .btn-login:hover {
      transform: translateY(-1px);
      box-shadow: 0 8px 25px rgba(99,102,241,0.4);
    }

    .btn-login:hover::before { opacity: 1; }
    .btn-login:active { transform: translateY(0); }

    /* Security note */
    .security-note {
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 0.4rem;
      font-size: 0.73rem;
      color: #475569;
      margin-top: 1.25rem;
    }

    /* Animations */
    @keyframes fadeDown {
      from { opacity:0; transform: translateY(-15px); }
      to   { opacity:1; transform: translateY(0); }
    }
    @keyframes fadeUp {
      from { opacity:0; transform: translateY(15px); }
      to   { opacity:1; transform: translateY(0); }
    }

    /* Particles */
    .particle {
      position: fixed;
      border-radius: 50%;
      animation: float linear infinite;
      pointer-events: none;
      opacity: 0.4;
    }
    @keyframes float {
      0%   { transform: translateY(100vh) rotate(0deg);   opacity: 0.4; }
      100% { transform: translateY(-10vh)  rotate(360deg); opacity: 0; }
    }
  </style>
</head>
<body>
  <!-- Floating particles -->
  <script>
    for (let i = 0; i < 12; i++) {
      const p = document.createElement('div');
      p.className = 'particle';
      const size = Math.random() * 4 + 2;
      p.style.cssText = `
        width:${size}px; height:${size}px;
        left:${Math.random()*100}vw;
        animation-duration:${Math.random()*15+10}s;
        animation-delay:${Math.random()*10}s;
        background: ${Math.random()>0.5 ? '#6366f1' : '#f59e0b'};
      `;
      document.body.appendChild(p);
    }
  </script>

  <div class="login-wrapper">
    <!-- Logo -->
    <div class="admin-badge">
      <div class="icon">🔐</div>
      <div class="label">
        <h1>AuctionHub</h1>
        <span>Admin Control Panel</span>
      </div>
    </div>

    <!-- Card -->
    <div class="login-card">
      <div class="card-header">
        <h2>Administrator Sign In</h2>
        <p>Restricted access — authorized personnel only</p>
      </div>
      <div class="divider"></div>

      <!-- Error -->
      <c:if test="${not empty error}">
        <div class="alert-error">⚠️ ${error}</div>
      </c:if>

      <form action="${pageContext.request.contextPath}/AdminLoginServlet" method="post">
        <div class="form-group">
          <label>Admin Username</label>
          <div class="input-wrap">
            <span class="ico">👤</span>
            <input type="text" name="username" id="admin-username"
                   placeholder="Enter admin username" required autocomplete="off">
          </div>
        </div>

        <div class="form-group">
          <label>Admin Password</label>
          <div class="input-wrap">
            <span class="ico">🔑</span>
            <input type="password" name="password" id="admin-password"
                   placeholder="Enter admin password" required>
          </div>
        </div>

        <button type="submit" class="btn-login">🚀 Access Admin Panel</button>
      </form>

      <div class="security-note">
        🔒 Secured · All actions are logged and monitored
      </div>
    </div>
  </div>
</body>
</html>
