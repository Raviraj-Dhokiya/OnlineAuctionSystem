<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <title>Search: ${searchQuery} — Auction System</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<nav class="navbar">
  <div class="nav-brand">🏆 AuctionHub</div>
  <div class="nav-links">
    <a href="${pageContext.request.contextPath}/DashboardServlet" class="btn btn-ghost btn-sm">← Home</a>
    <a href="${pageContext.request.contextPath}/LogoutServlet"    class="btn btn-ghost btn-sm">Logout</a>
  </div>
</nav>

<div class="container">
  <%-- ADVANCED SEARCH FILTER BAR --%>
  <div class="filter-panel card-body" style="background:#fff; border-radius:12px; box-shadow:0 4px 16px rgba(0,0,0,0.08); padding:20px; margin-bottom:24px;">
    <form action="${pageContext.request.contextPath}/SearchServlet" method="get" class="filter-form">
      
      <div class="form-row" style="grid-template-columns: 2fr 1fr 1fr 1fr 1fr; align-items: end;">
        
        <div class="form-group" style="margin-bottom:0;">
          <label for="q">Search Keyword</label>
          <input type="text" id="q" name="q" value="${searchQuery}" placeholder="eg. Phone, Guitar...">
        </div>

        <div class="form-group" style="margin-bottom:0;">
          <label for="category">Category</label>
          <select id="category" name="category">
            <option value="ALL">All Categories</option>
            <c:forEach var="cat" items="${categories}">
              <option value="${cat}" ${searchCategory == cat ? 'selected' : ''}>${cat}</option>
            </c:forEach>
          </select>
        </div>

        <div class="form-group" style="margin-bottom:0;">
          <label for="minPrice">Min Price (₹)</label>
          <input type="number" id="minPrice" name="minPrice" value="${searchMinPrice}" min="0" placeholder="0">
        </div>

        <div class="form-group" style="margin-bottom:0;">
          <label for="maxPrice">Max Price (₹)</label>
          <input type="number" id="maxPrice" name="maxPrice" value="${searchMaxPrice}" min="0" placeholder="Max">
        </div>

        <div class="form-group" style="margin-bottom:0;">
          <label for="sortBy">Sort By</label>
          <select id="sortBy" name="sortBy">
            <option value="ending_soon" ${searchSortBy == 'ending_soon' ? 'selected' : ''}>Ending Soon</option>
            <option value="newest"      ${searchSortBy == 'newest' ? 'selected' : ''}>Newest Listed</option>
            <option value="price_asc"   ${searchSortBy == 'price_asc' ? 'selected' : ''}>Price: Low to High</option>
            <option value="price_desc"  ${searchSortBy == 'price_desc' ? 'selected' : ''}>Price: High to Low</option>
          </select>
        </div>

      </div>
      
      <div style="margin-top:16px; text-align:right;">
        <a href="${pageContext.request.contextPath}/SearchServlet" class="btn btn-ghost" style="margin-right:10px;">Clear Filters</a>
        <button type="submit" class="btn btn-primary">Apply Filters & Search</button>
      </div>

    </form>
  </div>

  <h2 class="section-title">
    Results
    <c:if test="${not empty searchQuery}"> for "<c:out value="${searchQuery}"/>"</c:if>
    — ${searchResults.size()} found
  </h2>

  <div class="auction-grid">
    <c:choose>
      <c:when test="${empty searchResults}">
        <p class="empty-msg" style="grid-column: 1 / -1; text-align:center; padding: 40px; background:#fff; border-radius:12px;">
          No auctions found matching your criteria. Try adjusting the filters.
        </p>
      </c:when>
      <c:otherwise>
        <c:forEach var="item" items="${searchResults}">
          <div class="auction-card">
            <c:choose>
              <c:when test="${not empty item.imageName}">
                <img src="${pageContext.request.contextPath}/AuctionItemServlet?action=image&itemId=${item.itemId}"
                     alt="${item.title}" class="item-img">
              </c:when>
              <c:otherwise>
                <div class="item-img-placeholder">📦</div>
              </c:otherwise>
            </c:choose>
            <div class="card-body" style="position: relative;">
              <span class="category-badge">${item.category}</span>
              <span class="bid-count-badge" style="position:absolute; top:16px; right:16px; background:#f0f2f5; color:#666; font-size:0.75rem; padding:4px 8px; border-radius:12px; font-weight:600;">
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
                 class="btn btn-primary btn-full">Bid Now</a>
            </div>
          </div>
        </c:forEach>
      </c:otherwise>
    </c:choose>
  </div>
</div>
</body>
</html>
