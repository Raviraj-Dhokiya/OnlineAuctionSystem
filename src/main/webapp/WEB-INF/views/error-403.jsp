<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html>
<head><title>403 — Access Denied</title>
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body class="auth-page">
<div class="auth-container">
  <div class="auth-card" style="text-align:center;">
    <h1 style="font-size:4rem;">403</h1>
    <h2>Access Denied</h2>
    <p style="color:#888;margin:12px 0 20px;">You don't have permission to view this page.</p>
    <a href="${pageContext.request.contextPath}/DashboardServlet" class="btn btn-primary">Go to Dashboard</a>
  </div>
</div>
</body>
</html>
