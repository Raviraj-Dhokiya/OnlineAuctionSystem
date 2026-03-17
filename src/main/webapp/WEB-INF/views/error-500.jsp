<%@ page contentType="text/html;charset=UTF-8" isErrorPage="true" %>
<!DOCTYPE html>
<html>
<head><title>500 — Server Error</title>
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body class="auth-page">
<div class="auth-container">
  <div class="auth-card" style="text-align:center;">
    <h1 style="font-size:4rem;">500</h1>
    <h2>Server Error</h2>
    <p style="color:#888;margin:12px 0 8px;">Something went wrong on our end.</p>
    <p style="color:#aaa;font-size:0.85rem;margin-bottom:20px;">${pageContext.exception.message}</p>
    <a href="${pageContext.request.contextPath}/DashboardServlet" class="btn btn-primary">Go to Dashboard</a>
  </div>
</div>
</body>
</html>
