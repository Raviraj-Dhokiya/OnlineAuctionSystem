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
      <p>Sign in to start bidding</p>
    </div>

    <c:if test="${not empty error}">
      <div class="alert alert-error">${error}</div>
    </c:if>
    <c:if test="${not empty success}">
      <div class="alert alert-success">${success}</div>
    </c:if>

    <form action="${pageContext.request.contextPath}/LoginServlet" method="post">
      <div class="form-group">
        <label for="username">Username</label>
        <input type="text" id="username" name="username"
               value="${rememberedUsername}"
               placeholder="Enter your username" required>
      </div>
      <div class="form-group">
        <label for="password">Password</label>
        <input type="password" id="password" name="password"
               placeholder="Enter your password" required>
      </div>
      <div class="form-group checkbox-row">
        <label>
          <input type="checkbox" name="rememberMe"> Remember me
        </label>
      </div>
      <button type="submit" class="btn btn-primary btn-full">Login</button>
    </form>

    <p class="auth-footer">
      Don't have an account?
      <a href="${pageContext.request.contextPath}/RegisterServlet">Register here</a>
    </p>
  </div>
</div>

</body>
</html>
