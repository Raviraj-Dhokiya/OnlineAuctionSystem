<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <title>404 — Page Not Found</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body class="auth-page">
  <div class="auth-container" style="text-align:center;">
    <h1 style="font-size:5rem; color:#6c63ff; margin-bottom:0;">404</h1>
    <h2 style="color:#333; margin-bottom:12px;">Page Not Found</h2>
    <p style="color:#888; margin-bottom:24px;">The page you're looking for doesn't exist or has been moved.</p>
    <a href="${pageContext.request.contextPath}/DashboardServlet" class="btn btn-primary">🏠 Go to Dashboard</a>
  </div>
</body>
</html>
