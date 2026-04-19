<%@ page contentType="text/html;charset=UTF-8" isErrorPage="true" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <title>500 — Server Error</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body class="auth-page">
  <div class="auth-container" style="text-align:center;">
    <h1 style="font-size:5rem; color:#e67e22; margin-bottom:0;">500</h1>
    <h2 style="color:#333; margin-bottom:12px;">Server Error</h2>
    <p style="color:#888; margin-bottom:8px;">Something went wrong on our end. Please try again later.</p>
    <% if (pageContext.getException() != null && pageContext.getException().getMessage() != null) { %>
      <p style="font-size:0.85rem; color:#aaa; background:#f9f9f9; padding:10px; border-radius:6px; margin-bottom:20px;">${pageContext.exception.message}</p>
    <% } %>
    <a href="${pageContext.request.contextPath}/DashboardServlet" class="btn btn-primary">🏠 Go to Dashboard</a>
  </div>
</body>
</html>
