<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <title>Search Results — Auction System</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<nav class="navbar">
  <div class="nav-brand">
    <img src="https://thumbs.dreamstime.com/b/online-auction-gavel-internet-bidding-web-site-win-buy-item-d-words-wood-block-closing-website-42430139.jpg" alt="AuctionHub Logo" style="height:36px; width:36px; object-fit:cover; border-radius:8px; margin-right:8px; vertical-align:middle;">
    AuctionHub
  </div>
  <div class="nav-links">
    <a href="${pageContext.request.contextPath}/DashboardServlet" class="btn btn-ghost btn-sm">← Home</a>
    <a href="${pageContext.request.contextPath}/LogoutServlet"    class="btn btn-ghost btn-sm">🚪 Logout</a>
  </div>
</nav>

<div class="container">

  <%-- ADVANCED SEARCH / FILTER FORM --%>
  <div style="background:#fff; border:1px solid #e0e0e0; border-radius:10px; padding:20px; margin-bottom:24px;">
    <form action="${pageContext.request.contextPath}/SearchServlet" method="get">
      <div style="display:grid; grid-template-columns: 2fr 1fr 1fr 1fr 1fr; gap:12px; align-items:end;">
        <div class="form-group" style="margin:0;">
          <label>🔍 Search Keyword</label>
          <input type="text" name="q" value="${searchQuery}" placeholder="e.g. Phone, Guitar...">
        </div>
        <div class="form-group" style="margin:0;">
          <label>📂 Category</label>
          <select name="category">
            <option value="ALL">All Categories</option>
            <c:forEach var="cat" items="${categories}">
              <option value="${cat}" ${searchCategory == cat ? 'selected' : ''}>${cat}</option>
            </c:forEach>
          </select>
        </div>
        <div class="form-group" style="margin:0;">
          <label>💰 Min Price (₹)</label>
          <input type="number" name="minPrice" value="${searchMinPrice}" min="0" placeholder="0">
        </div>
        <div class="form-group" style="margin:0;">
          <label>💰 Max Price (₹)</label>
          <input type="number" name="maxPrice" value="${searchMaxPrice}" min="0" placeholder="Max">
        </div>
        <div class="form-group" style="margin:0;">
          <label>🔄 Sort By</label>
          <select name="sortBy">
            <option value="ending_soon" ${searchSortBy == 'ending_soon' ? 'selected' : ''}>Ending Soon</option>
            <option value="newest"      ${searchSortBy == 'newest'      ? 'selected' : ''}>Newest Listed</option>
            <option value="price_asc"   ${searchSortBy == 'price_asc'   ? 'selected' : ''}>Price: Low → High</option>
            <option value="price_desc"  ${searchSortBy == 'price_desc'  ? 'selected' : ''}>Price: High → Low</option>
          </select>
        </div>
      </div>
      <div style="margin-top:14px; text-align:right; display:flex; gap:8px; justify-content:flex-end;">
        <a href="${pageContext.request.contextPath}/SearchServlet" class="btn btn-ghost btn-sm">✖ Clear Filters</a>
        <button type="submit" class="btn btn-primary btn-sm">🔍 Apply &amp; Search</button>
      </div>
    </form>
  </div>

  <%-- RESULTS HEADER --%>
  <h2 class="section-title">
    Search Results
    <c:if test="${not empty searchQuery}"> for "<c:out value="${searchQuery}"/>"</c:if>
    <span style="font-size:0.8rem; font-weight:400; color:#888; margin-left:8px;">(${searchResults.size()} found)</span>
  </h2>

  <%-- AUCTION GRID --%>
  <div class="auction-grid">
    <c:choose>
      <c:when test="${empty searchResults}">
        <p class="empty-msg" style="grid-column: 1 / -1;">
          No auctions found matching your criteria. Try adjusting the filters.
        </p>
      </c:when>
      <c:otherwise>
        <c:forEach var="item" items="${searchResults}">
          <div class="auction-card">
            <img src="${pageContext.request.contextPath}/AuctionItemServlet?action=image&itemId=${item.itemId}"
                 alt="${item.title}" class="item-img"
                 onerror="this.style.display='none';"
            >
            <div class="card-body" style="position: relative;">
              <span class="category-badge">${item.category}</span>
              <span style="position:absolute; top:16px; right:16px; background:#f0f2f5; color:#666; font-size:0.75rem; padding:4px 8px; border-radius:12px; font-weight:600;">
                🔥 ${item.bidCount} Bids
              </span>
              <h3 class="item-title">${item.title}</h3>
              <p class="item-desc">${item.description}</p>
              <div class="price-row">
                <span class="current-price">₹<fmt:formatNumber value="${item.currentPrice}" pattern="#,##0.00"/></span>
                <span class="price-label">Current bid</span>
              </div>
              <div class="time-row">
                Ends: <fmt:formatDate value="${item.endTime}" pattern="dd MMM yyyy HH:mm"/>
              </div>
              <a href="${pageContext.request.contextPath}/BidServlet?itemId=${item.itemId}"
                 class="btn btn-primary btn-full">🔨 Bid Now</a>
            </div>
          </div>
        </c:forEach>
      </c:otherwise>
    </c:choose>
  </div>

</div>
</body>
</html>
