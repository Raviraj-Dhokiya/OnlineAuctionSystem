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
  <div class="search-bar">
    <form action="${pageContext.request.contextPath}/SearchServlet" method="get">
      <input type="text" name="q" value="${searchQuery}" placeholder="Search auctions...">
      <button type="submit" class="btn btn-primary">Search</button>
    </form>
  </div>

  <h2 class="section-title">
    Results for "<c:out value="${searchQuery}"/>"
    — ${searchResults.size()} found
  </h2>

  <div class="auction-grid">
    <c:choose>
      <c:when test="${empty searchResults}">
        <p class="empty-msg">No auctions found matching your search. Try different keywords.</p>
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
            <div class="card-body">
              <span class="category-badge">${item.category}</span>
              <h3 class="item-title">${item.title}</h3>
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
