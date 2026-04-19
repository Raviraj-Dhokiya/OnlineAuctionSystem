<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <title>Payment — Auction System</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
  <style>
    .payment-container {
      max-width: 500px;
      margin: 80px auto;
      background: white;
      padding: 30px 40px;
      border-radius: 8px;
      box-shadow: 0 4px 12px rgba(0,0,0,0.1);
      text-align: center;
    }
    .payment-icon {
      font-size: 4rem;
      margin-bottom: 20px;
    }
    .payment-details {
      margin: 20px 0 30px 0;
      font-size: 1.2rem;
      text-align: left;
      background: #f9f9f9;
      padding: 20px;
      border-radius: 8px;
    }
    .payment-details div {
      margin-bottom: 12px;
      display: flex;
      justify-content: space-between;
    }
    .btn-pay {
      background-color: #28a745;
      color: #ffffff !important;
      -webkit-text-fill-color: #ffffff !important;
      font-size: 1.2rem;
      font-weight: bold;
      padding: 14px 24px;
      border: none;
      border-radius: 6px;
      cursor: pointer;
      width: 100%;
      transition: background-color 0.3s ease;
      text-shadow: none !important;
    }
    .btn-pay:hover {
      background-color: #218838;
    }
    .cancel-link {
      display: inline-block;
      margin-top: 20px;
      color: #777;
      text-decoration: underline;
    }
    .cancel-link:hover {
      color: #333;
    }
  </style>
</head>
<body>

<nav class="navbar">
  <div class="nav-brand">
    <img src="https://thumbs.dreamstime.com/b/online-auction-gavel-internet-bidding-web-site-win-buy-item-d-words-wood-block-closing-website-42430139.jpg" alt="AuctionHub Logo" style="height:36px; width:36px; object-fit:cover; border-radius:8px; margin-right:8px; vertical-align:middle;">
    AuctionHub
  </div>
  <div class="nav-links">
    <a href="${pageContext.request.contextPath}/DashboardServlet" class="btn btn-ghost btn-sm">✕ Cancel</a>
  </div>
</nav>

<div class="container">
  <div class="payment-container">
    <div class="payment-icon">💳</div>
    <h2>Complete Your Payment</h2>
    <p style="color: #666; margin-bottom: 20px;">Click the button below to complete your mock payment securely.</p>
    
    <div class="payment-details">
      <div>
        <strong>Item Name:</strong>
        <span>${title}</span>
      </div>
      <div style="border-top: 1px solid #eee; padding-top: 12px; margin-top: 12px;">
        <strong>Total Amount:</strong>
        <span style="font-size: 1.3rem; color: #28a745; font-weight: bold;">
          ₹<fmt:formatNumber value="${amount}" pattern="#,##0.00"/>
        </span>
      </div>
    </div>
    
    <form action="${pageContext.request.contextPath}/PaymentServlet" method="post">
      <input type="hidden" name="winnerId" value="${winnerId}">
      <button type="submit" class="btn-pay" style="color:#fff !important;">💳 Pay ₹<fmt:formatNumber value="${amount}" pattern="#,##0.00"/> Now</button>
    </form>
    
    <a href="${pageContext.request.contextPath}/DashboardServlet" class="cancel-link">Back to Dashboard</a>
  </div>
</div>

</body>
</html>
