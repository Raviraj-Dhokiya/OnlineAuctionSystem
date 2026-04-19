<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Login — Auction System</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body class="auth-page">

<div class="auth-container">
  <div class="auth-card">

    <div class="auth-header">
      <h1>🏆 AuctionHub</h1>
      <p>Sign in to your account</p>
    </div>

    <c:if test="${not empty error}">
      <div class="alert alert-error">${error}</div>
    </c:if>
    <c:if test="${not empty success}">
      <div class="alert alert-success">${success}</div>
    </c:if>

    <form action="${pageContext.request.contextPath}/LoginServlet" method="post">
      <input type="hidden" name="csrfToken" value="${csrfToken}">

      <div class="form-group">
        <label for="usernameInput">🔖 Username</label>
        <input type="text" id="usernameInput" name="username"
               placeholder="Enter your username" required autofocus>
      </div>

      <div class="form-group">
        <label for="passwordInput">🔑 Password</label>
        <input type="password" id="passwordInput" name="password"
               placeholder="Enter your password" required>
      </div>

      <button type="submit" class="btn btn-primary btn-full">🔐 Login</button>
    </form>

    <p class="auth-footer" style="margin-top: 14px;">
      Don't have an account?
      <a href="${pageContext.request.contextPath}/RegisterServlet">Register</a>
    </p>

    <div style="display:flex; align-items:center; gap:10px; margin-top:18px;">
      <div style="flex:1; height:1px; background:#e0e0e0;"></div>
      <span style="color:#bbb; font-size:0.78rem; white-space:nowrap;">Admin Access</span>
      <div style="flex:1; height:1px; background:#e0e0e0;"></div>
    </div>

    <a href="${pageContext.request.contextPath}/AdminLoginServlet"
       style="display:flex; align-items:center; justify-content:center; gap:8px;
              margin-top:12px; padding:10px 0; width:100%;
              background:#fff4e6; border:1.5px solid #e67e22; border-radius:8px;
              color:#c0580a; font-weight:700; font-size:0.9rem;
              text-decoration:none; transition:background 0.2s;"
       onmouseover="this.style.background='#ffe8cc'" onmouseout="this.style.background='#fff4e6'">
      &#x1F510; Admin Login
    </a>

  </div>
</div>

</body>
</html>