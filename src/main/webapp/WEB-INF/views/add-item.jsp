<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <title>List New Item — Auction System</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<nav class="navbar">
  <div class="nav-brand">🏆 AuctionHub</div>
  <div class="nav-links">
    <a href="${pageContext.request.contextPath}/DashboardServlet" class="btn btn-ghost btn-sm">← Dashboard</a>
    <a href="${pageContext.request.contextPath}/LogoutServlet" class="btn btn-ghost btn-sm">Logout</a>
  </div>
</nav>

<div class="container narrow">
  <h2 class="section-title">List an Item for Auction</h2>

  <c:if test="${not empty error}">
    <div class="alert alert-error">${error}</div>
  </c:if>

  <%-- multipart/form-data required for file upload (Unit 6) --%>
  <form action="${pageContext.request.contextPath}/AuctionItemServlet"
        method="post" enctype="multipart/form-data" class="form-card">

    <div class="form-group">
      <label>Item Title *</label>
      <input type="text" name="title" placeholder="e.g. Vintage Guitar 1965" required>
    </div>
    <div class="form-group">
      <label>Description</label>
      <textarea name="description" rows="4"
                placeholder="Describe your item..."></textarea>
    </div>
    <div class="form-row">
      <div class="form-group">
        <label>Category *</label>
        <select name="category" required>
          <option value="">-- Select --</option>
          <option>Electronics</option>
          <option>Vehicles</option>
          <option>Art</option>
          <option>Furniture</option>
          <option>Collectibles</option>
          <option>Music</option>
          <option>Other</option>
        </select>
      </div>
      <div class="form-group">
        <label>Starting Price (₹) *</label>
        <input type="number" name="startingPrice" min="1" step="0.01" required>
      </div>
    </div>
    <div class="form-row">
      <div class="form-group">
        <label>Reserve Price (₹) <small>(optional)</small></label>
        <input type="number" name="reservePrice" min="0" step="0.01">
      </div>
      <div class="form-group">
        <label>Auction End Date & Time *</label>
        <input type="datetime-local" name="endTime" required>
      </div>
    </div>
    <div class="form-group">
      <label>Item Image <small>(JPG/PNG, max 5MB)</small></label>
      <input type="file" name="itemImage" accept="image/*">
    </div>

    <button type="submit" class="btn btn-primary btn-full">List Item for Auction</button>
  </form>
</div>
</body>
</html>
