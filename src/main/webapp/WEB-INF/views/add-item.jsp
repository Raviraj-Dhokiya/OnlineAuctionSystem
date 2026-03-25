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
  <div class="nav-brand">
    <img src="https://thumbs.dreamstime.com/b/online-auction-gavel-internet-bidding-web-site-win-buy-item-d-words-wood-block-closing-website-42430139.jpg" alt="AuctionHub Logo" style="height:36px; width:36px; object-fit:cover; border-radius:8px; margin-right:8px; vertical-align:middle;">
    AuctionHub
  </div>
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
  <c:if test="${not empty errorDetail}">
    <div class="alert alert-error" style="font-size:0.88rem;">
      <strong>Note:</strong> If listing fails, it may be due to a database storage limit on the free Oracle plan.
      Try listing the item <strong>without an image</strong>, or use a smaller image (&lt;500KB).
    </div>
  </c:if>

  <%-- multipart/form-data required for file upload (Unit 6) --%>
  <form action="${pageContext.request.contextPath}/AuctionItemServlet"
        method="post" enctype="multipart/form-data" class="form-card">

    <%-- BUG #4 FIX: CSRF token hidden field — AuctionItemServlet.doPost() verify karega --%>
    <input type="hidden" name="csrfToken" value="${csrfToken}">

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
          <option>Fashion & Accessories</option>
          <option>Real Estate</option>
          <option>Home Appliances</option>
          <option>Sports & Outdoors</option>
          <option>Toys & Hobbies</option>
          <option>Jewelry & Watches</option>
          <option>Antiques</option>
          <option>Books & Comics</option>
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
      <label>Item Images <small>(JPG/PNG, max 5 photos, max 2MB each)</small></label>
      <input type="file" name="itemImage" accept="image/*" multiple id="imageInput">
      <small style="color:#666; display:block; margin-top:5px;">First photo will be the cover image. Select up to 5 for the gallery slider.</small>
    </div>

    <!-- JS Limit enforcement -->
    <script>
      document.getElementById('imageInput').addEventListener('change', function(e) {
        if (this.files.length > 5) {
          alert('You can upload a maximum of 5 photos!');
          this.value = ''; // clears the selection
        }
      });
    </script>

    <button type="submit" class="btn btn-primary btn-full">List Item for Auction</button>
  </form>
</div>
</body>
</html>
