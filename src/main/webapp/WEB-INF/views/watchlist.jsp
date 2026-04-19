<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <title>My Watchlist — Auction System</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
  <style>
    .watch-card {
      background: white;
      border: 1px solid #e0e0e0;
      border-radius: 10px;
      padding: 18px 20px;
      display: flex;
      flex-direction: column;
      gap: 8px;
      transition: box-shadow 0.2s;
    }
    .watch-card:hover { box-shadow: 0 4px 16px rgba(0,0,0,0.1); }
    .watch-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
      gap: 16px;
      margin-top: 16px;
    }
    .watch-title {
      font-size: 1.05rem;
      font-weight: 700;
      color: #1a1a2e;
      text-decoration: none;
    }
    .watch-title:hover { color: #6c63ff; }
    .watch-price {
      font-size: 1.3rem;
      font-weight: 800;
      color: #6c63ff;
    }
    .watch-meta { font-size: 0.82rem; color: #888; }
    .watch-actions { display: flex; gap: 8px; margin-top: 6px; }
    .empty-watchlist { text-align: center; padding: 60px 20px; color: #888; }
    .empty-watchlist .ei { font-size: 3.5rem; display: block; margin-bottom: 12px; }
  </style>
</head>
<body>

<nav class="navbar">
  <div class="nav-brand">
    <img src="https://thumbs.dreamstime.com/b/online-auction-gavel-internet-bidding-web-site-win-buy-item-d-words-wood-block-closing-website-42430139.jpg" alt="AuctionHub Logo" style="height:36px; width:36px; object-fit:cover; border-radius:8px; margin-right:8px; vertical-align:middle;">
    AuctionHub
  </div>
  <div class="nav-links">
    <a href="${pageContext.request.contextPath}/DashboardServlet" class="btn btn-ghost btn-sm">← Dashboard</a>
    <span>Welcome, <strong>${sessionScope.username}</strong></span>
    <a href="${pageContext.request.contextPath}/LogoutServlet" class="btn btn-ghost btn-sm">🚪 Logout</a>
  </div>
</nav>

<div class="container">

  <h2 class="section-title">⭐ My Watchlist</h2>
  <p style="color:#888; margin-bottom:8px; font-size:0.9rem;">Items you are tracking — stay updated on their bids.</p>

  <c:choose>
    <c:when test="${empty watchlist}">
      <div class="empty-watchlist">
        <span class="ei">📭</span>
        <p>Your watchlist is empty.</p>
        <p style="font-size:0.9rem; margin-bottom:16px;">
          Go to any auction item and click <strong>"Add to Watchlist"</strong> to track it here.
        </p>
        <a href="${pageContext.request.contextPath}/DashboardServlet" class="btn btn-primary">🔍 Browse Auctions</a>
      </div>
    </c:when>
    <c:otherwise>
      <p style="color:#888; font-size:0.9rem;">Tracking <strong>${watchlist.size()}</strong> item(s).</p>
      <div class="watch-grid">
        <c:forEach var="w" items="${watchlist}">
          <div class="watch-card">

            <span class="badge ${w.itemStatus == 'ACTIVE' ? 'badge-success' :
                                  w.itemStatus == 'CLOSED' ? 'badge-info' : 'badge-warn'}">
              ${w.itemStatus}
            </span>

            <a href="${pageContext.request.contextPath}/BidServlet?itemId=${w.itemId}"
               class="watch-title">
              ${w.itemTitle}
            </a>

            <div class="watch-price">
              ₹<fmt:formatNumber value="${w.itemCurrentPrice}" pattern="#,##0.00"/>
            </div>

            <div class="watch-meta">
              <c:choose>
                <c:when test="${w.itemStatus == 'ACTIVE'}">
                  ⏰ Ends: <fmt:formatDate value="${w.itemEndTime}" pattern="dd MMM yyyy, HH:mm"/>
                </c:when>
                <c:otherwise>🏁 Auction Ended</c:otherwise>
              </c:choose>
            </div>

            <div class="watch-meta">
              Added: <fmt:formatDate value="${w.addedAt}" pattern="dd MMM yyyy"/>
            </div>

            <div class="watch-actions">
              <a href="${pageContext.request.contextPath}/BidServlet?itemId=${w.itemId}"
                 class="btn btn-sm btn-primary">👁️ View</a>
              <a href="${pageContext.request.contextPath}/WatchlistServlet?action=remove&itemId=${w.itemId}"
                 class="btn btn-sm btn-ghost"
                 onclick="return confirm('Remove from watchlist?')">🗑️ Remove</a>
            </div>

          </div>
        </c:forEach>
      </div>
    </c:otherwise>
  </c:choose>

</div>
</body>
</html>
