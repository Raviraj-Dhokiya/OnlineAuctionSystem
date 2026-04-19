<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <title>Dashboard — Auction System</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>

<%-- NAVBAR --%>
<nav class="navbar">
  <div class="nav-brand">
    <img src="https://thumbs.dreamstime.com/b/online-auction-gavel-internet-bidding-web-site-win-buy-item-d-words-wood-block-closing-website-42430139.jpg" alt="AuctionHub Logo" style="height:36px; width:36px; object-fit:cover; border-radius:8px; margin-right:8px; vertical-align:middle;">
    AuctionHub
  </div>
  <div class="nav-links">
    <span>Welcome, <strong>${sessionScope.username}</strong></span>
    <a href="${pageContext.request.contextPath}/DashboardServlet" class="btn btn-ghost btn-sm">🏠 Home</a>
    <a href="${pageContext.request.contextPath}/ProfileServlet"   class="btn btn-outline btn-sm">👤 Profile</a>
    <a href="${pageContext.request.contextPath}/AuctionItemServlet" class="btn btn-outline btn-sm">➕ List Item</a>
    <a href="${pageContext.request.contextPath}/LogoutServlet"      class="btn btn-ghost btn-sm">🚪 Logout</a>
  </div>
</nav>

<div class="container">

  <%-- SEARCH BAR --%>
  <div class="search-bar">
    <form action="${pageContext.request.contextPath}/SearchServlet" method="get">
      <input type="text" name="q" placeholder="Search auctions...">
      <button type="submit" class="btn btn-primary">🔍 Search</button>
    </form>
  </div>

  <%-- ACTIVE AUCTIONS GRID --%>
  <h2 class="section-title">🔥 Live Auctions</h2>

  <div class="auction-grid">
    <c:choose>
      <c:when test="${empty activeItems}">
        <p class="empty-msg">No active auctions right now. Be the first to list one!</p>
      </c:when>
      <c:otherwise>
        <c:forEach var="item" items="${activeItems}">
          <div class="auction-card">
            <%-- Item image --%>
            <img src="${pageContext.request.contextPath}/AuctionItemServlet?action=image&itemId=${item.itemId}"
                 alt="${item.title}" class="item-img"
                 onerror="this.style.display='none'; var ph=document.createElement('div'); ph.className='item-img-placeholder'; ph.textContent='\uD83D\uDCE6'; this.parentNode.insertBefore(ph, this);"
            >

            <div class="card-body" style="position: relative;">
              <span class="category-badge">${item.category}</span>
              <span class="bid-count-badge" style="position:absolute; top:16px; right:16px; background:#f0f2f5; color:#666; font-size:0.75rem; padding:4px 8px; border-radius:12px; font-weight:600;">
                🔥 ${item.bidCount} Bids
              </span>
              <h3 class="item-title">${item.title}</h3>
              <p class="item-desc">${item.description}</p>

              <div class="price-row">
                <span class="current-price">
                  ₹<fmt:formatNumber value="${item.currentPrice}" pattern="#,##0.00"/>
                </span>
                <span class="price-label">Current bid</span>
              </div>

              <div class="time-row">
                <span class="ends-label">Ends:</span>
                <span class="ends-time">
                  <fmt:formatDate value="${item.endTime}" pattern="dd MMM yyyy HH:mm"/>
                </span>
              </div>

              <a href="${pageContext.request.contextPath}/BidServlet?itemId=${item.itemId}"
                 class="btn btn-primary btn-full">🔨 Bid Now</a>
            </div>
          </div>
        </c:forEach>
      </c:otherwise>
    </c:choose>
  </div>

  <%-- MY BID HISTORY --%>
  <h2 class="section-title">📋 My Bid History</h2>

  <div class="table-wrap">
    <c:choose>
      <c:when test="${empty myBids}">
        <p class="empty-msg">You haven't placed any bids yet.</p>
      </c:when>
      <c:otherwise>
        <table class="data-table">
          <thead>
            <tr>
              <th>Item</th>
              <th>My Bid (₹)</th>
              <th>Time</th>
              <th>Status</th>
            </tr>
          </thead>
          <tbody>
            <c:forEach var="bid" items="${myBids}">
              <tr>
                <td><a href="${pageContext.request.contextPath}/BidServlet?itemId=${bid.itemId}">${bid.itemTitle}</a></td>
                <td><fmt:formatNumber value="${bid.bidAmount}" pattern="#,##0.00"/></td>
                <td><fmt:formatDate value="${bid.bidTime}" pattern="dd MMM HH:mm"/></td>
                <td>
                  <c:choose>
                    <c:when test="${bid.winning}">
                      <span class="badge badge-success">Winning</span>
                    </c:when>
                    <c:otherwise>
                      <span class="badge badge-neutral">Placed</span>
                    </c:otherwise>
                  </c:choose>
                </td>
              </tr>
            </c:forEach>
          </tbody>
        </table>
      </c:otherwise>
    </c:choose>
  </div>

  <%-- MY WINS --%>
  <h2 class="section-title">🏆 Auctions I Won</h2>
  <div class="table-wrap">
    <c:choose>
      <c:when test="${empty myWins}">
        <p class="empty-msg">No wins yet. Keep bidding!</p>
      </c:when>
      <c:otherwise>
        <table class="data-table">
          <thead>
            <tr><th>Item</th><th>Winning Bid (₹)</th><th>Payment</th><th>Date</th></tr>
          </thead>
          <tbody>
            <c:forEach var="win" items="${myWins}">
              <tr>
                <td>${win.itemTitle}</td>
                <td><fmt:formatNumber value="${win.winningAmount}" pattern="#,##0.00"/></td>
                <td>
                  <c:choose>
                    <c:when test="${win.paymentStatus == 'PAID'}">
                      <span class="badge badge-success">Paid</span>
                    </c:when>
                    <c:otherwise>
                      <span class="badge badge-warn">Pending</span>
                      <a href="${pageContext.request.contextPath}/PaymentServlet?winnerId=${win.winnerId}&amount=${win.winningAmount}&title=${win.itemTitle}"
                         style="display:inline-block; background:#16a34a; color:#ffffff !important; font-weight:700; font-size:0.85rem; padding:6px 14px; border-radius:6px; text-decoration:none; margin-left:10px; border:2px solid #15803d;"
                      >💳 Pay Now</a>
                    </c:otherwise>
                  </c:choose>
                </td>
                <td><fmt:formatDate value="${win.awardedAt}" pattern="dd MMM yyyy"/></td>
              </tr>
            </c:forEach>
          </tbody>
        </table>
      </c:otherwise>
    </c:choose>
  </div>

</div><%-- .container --%>

<%-- Live Bid notification via Socket (Unit 3) --%>
<div id="notif-bar" class="notif-bar" style="display:none;"></div>

<script src="${pageContext.request.contextPath}/js/bid-live.js"></script>
</body>
</html>
