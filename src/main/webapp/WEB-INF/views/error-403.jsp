<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <title>403 — Access Denied</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body class="auth-page">
  <div class="auth-container" style="text-align:center;">
    <h1 style="font-size:5rem; color:#e74c3c; margin-bottom:0;">403</h1>
    <h2 style="color:#333; margin-bottom:12px;">Access Denied</h2>
    <p style="color:#888; margin-bottom:24px;">You don't have permission to view this page.</p>
    <a href="${pageContext.request.contextPath}/DashboardServlet" class="btn btn-primary">🏠 Go to Dashboard</a>
  </div>
</body>
</html>
