<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html>
<head><title>404 — Not Found</title>
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body class="auth-page">
<div class="auth-container">
  <div class="auth-card" style="text-align:center;">
    <h1 style="font-size:4rem;">404</h1>
    <h2>Page Not Found</h2>
    <p style="color:#888;margin:12px 0 20px;">The page you're looking for doesn't exist.</p>
    <a href="${pageContext.request.contextPath}/DashboardServlet" class="btn btn-primary">Go to Dashboard</a>
  </div>
</div>
</body>
</html>
