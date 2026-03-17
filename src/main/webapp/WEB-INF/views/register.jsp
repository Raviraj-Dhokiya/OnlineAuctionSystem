<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <title>Register — Auction System</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body class="auth-page">

<div class="auth-container">
  <div class="auth-card wide">
    <div class="auth-header">
      <h1>🏆 AuctionHub</h1>
      <p>Create your account</p>
    </div>

    <c:if test="${not empty error}">
      <div class="alert alert-error">${error}</div>
    </c:if>

    <form action="${pageContext.request.contextPath}/RegisterServlet" method="post">
      <div class="form-row">
        <div class="form-group">
          <label>Full Name</label>
          <input type="text" name="fullName" placeholder="John Doe" required>
        </div>
        <div class="form-group">
          <label>Username</label>
          <input type="text" name="username" placeholder="john_doe" required>
        </div>
      </div>
      <div class="form-row">
        <div class="form-group">
          <label>Email</label>
          <input type="email" name="email" placeholder="john@example.com" required>
        </div>
        <div class="form-group">
          <label>Phone</label>
          <input type="tel" name="phone" placeholder="+91 98765 43210" required>
        </div>
      </div>
      <div class="form-row">
        <div class="form-group">
          <label>Password</label>
          <input type="password" name="password"
                 placeholder="Min 8 chars, uppercase, digit, special" required>
        </div>
        <div class="form-group">
          <label>Confirm Password</label>
          <input type="password" name="confirmPassword"
                 placeholder="Repeat password" required>
        </div>
      </div>
      <button type="submit" class="btn btn-primary btn-full">Create Account</button>
    </form>

    <p class="auth-footer">
      Already have an account?
      <a href="${pageContext.request.contextPath}/LoginServlet">Login</a>
    </p>
  </div>
</div>

</body>
</html>
