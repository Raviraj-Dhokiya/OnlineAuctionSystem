<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <title>My Watchlist — AuctionHub</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
  <style>
    .watchlist-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
      gap: 1.5rem;
      margin-top: 1.5rem;
    }
    .watch-card {
      background: var(--card-bg, #1e293b);
      border: 1px solid rgba(255,255,255,0.08);
      border-radius: 12px;
      padding: 1.2rem;
      display: flex;
      flex-direction: column;
      gap: 0.6rem;
      transition: transform 0.2s, box-shadow 0.2s;
    }
    .watch-card:hover {
      transform: translateY(-3px);
      box-shadow: 0 8px 24px rgba(0,0,0,0.3);
    }
    .watch-card-title {
      font-size: 1.05rem;
      font-weight: 600;
      color: #f1f5f9;
      text-decoration: none;
    }
    .watch-card-title:hover { color: #60a5fa; }
    .watch-price {
      font-size: 1.3rem;
      font-weight: 700;
      color: #34d399;
    }
    .watch-meta {
      font-size: 0.82rem;
      color: #94a3b8;
    }
    .watch-actions {
      display: flex;
      gap: 0.5rem;
      margin-top: 0.3rem;
    }
    .empty-watchlist {
      text-align: center;
      padding: 4rem 2rem;
      color: #64748b;
    }
    .empty-watchlist .emoji { font-size: 3.5rem; display: block; margin-bottom: 1rem; }
  </style>
</head>
<body>

<nav class="navbar">
  <div class="nav-brand">🏆 AuctionHub</div>
  <div class="nav-links">
    <a href="${pageContext.request.contextPath}/DashboardServlet" class="btn btn-ghost btn-sm">← Dashboard</a>
    <span>Welcome, <strong>${sessionScope.username}</strong></span>
    <a href="${pageContext.request.contextPath}/LogoutServlet" class="btn btn-ghost btn-sm">Logout</a>
  </div>
</nav>

<div class="container">

  <h2 class="section-title">⭐ My Watchlist</h2>
  <p class="section-subtitle">Items you are tracking — stay updated on their bids.</p>

  <c:choose>
    <c:when test="${empty watchlist}">
      <div class="empty-watchlist">
        <span class="emoji">📭</span>
        <p>Your watchlist is empty.</p>
        <p>Go to an auction item and click <strong>"Add to Watchlist"</strong> to track it here.</p>
        <a href="${pageContext.request.contextPath}/DashboardServlet" class="btn btn-primary" style="margin-top:1rem;">
          Browse Auctions
        </a>
      </div>
    </c:when>
    <c:otherwise>
      <p style="color:#64748b; margin-bottom:0.5rem;">
        Tracking <strong>${watchlist.size()}</strong> item(s).
      </p>
      <div class="watchlist-grid">
        <c:forEach var="w" items="${watchlist}">
          <div class="watch-card">

            <%-- Status badge --%>
            <span class="badge ${w.itemStatus == 'ACTIVE' ? 'badge-success' :
                                  w.itemStatus == 'CLOSED' ? 'badge-info' : 'badge-warn'}">
              ${w.itemStatus}
            </span>

            <%-- Item Title (link to auction page) --%>
            <a href="${pageContext.request.contextPath}/BidServlet?itemId=${w.itemId}"
               class="watch-card-title">
              ${w.itemTitle}
            </a>

            <%-- Current Price --%>
            <div class="watch-price">
              ₹<fmt:formatNumber value="${w.itemCurrentPrice}" pattern="#,##0.00"/>
            </div>

            <%-- End time --%>
            <div class="watch-meta">
              <c:choose>
                <c:when test="${w.itemStatus == 'ACTIVE'}">
                  ⏰ Ends: <fmt:formatDate value="${w.itemEndTime}" pattern="dd MMM yyyy, HH:mm"/>
                </c:when>
                <c:otherwise>
                  🏁 Auction Ended
                </c:otherwise>
              </c:choose>
            </div>

            <%-- Added at --%>
            <div class="watch-meta">
              Added on: <fmt:formatDate value="${w.addedAt}" pattern="dd MMM yyyy"/>
            </div>

            <%-- Actions --%>
            <div class="watch-actions">
              <a href="${pageContext.request.contextPath}/BidServlet?itemId=${w.itemId}"
                 class="btn btn-sm btn-primary">View Auction</a>
              <a href="${pageContext.request.contextPath}/WatchlistServlet?action=remove&itemId=${w.itemId}"
                 class="btn btn-sm btn-outline"
                 onclick="return confirm('Remove from watchlist?')">Remove</a>
            </div>

          </div>
        </c:forEach>
      </div>
    </c:otherwise>
  </c:choose>

</div>
</body>
</html>
