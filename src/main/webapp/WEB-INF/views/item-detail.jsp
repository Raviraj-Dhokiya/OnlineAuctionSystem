<%@ page contentType="text/html;charset=UTF-8" %>
  <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
    <%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
      <!DOCTYPE html>
      <html lang="en">

      <head>
        <meta charset="UTF-8">
        <title>${item.title} — Auction System</title>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
      </head>

      <body>

        <nav class="navbar">
          <div class="nav-brand">🏆 AuctionHub</div>
          <div class="nav-links">
            <a href="${pageContext.request.contextPath}/DashboardServlet" class="btn btn-ghost btn-sm">← Back</a>
            <span>Welcome, <strong>${sessionScope.username}</strong></span>
            <a href="${pageContext.request.contextPath}/LogoutServlet" class="btn btn-ghost btn-sm">Logout</a>
          </div>
        </nav>

        <div class="container">

          <c:if test="${param.success == '1'}">
            <div class="alert alert-success">✓ Your bid was placed successfully!</div>
          </c:if>
          <c:if test="${param.error == 'bid_low'}">
            <div class="alert alert-error">Your bid must be higher than the current bid.</div>
          </c:if>

          <div class="item-detail-layout">

            <%-- Left: image + details --%>
              <div class="item-detail-left">
                <c:choose>
                  <c:when test="${not empty item.imageName}">
                    <img src="${pageContext.request.contextPath}/AuctionItemServlet?action=image&itemId=${item.itemId}"
                      alt="${item.title}" class="item-detail-img">
                  </c:when>
                  <c:otherwise>
                    <div class="item-detail-img-placeholder">📦</div>
                  </c:otherwise>
                </c:choose>

                <div class="item-meta">
                  <span class="category-badge">${item.category}</span>
                  <h1>${item.title}</h1>
                  <p class="seller-info">Listed by: <strong>${item.sellerName}</strong></p>
                  <p class="item-desc-full">${item.description}</p>

                  <div class="price-section">
                    <div class="price-box">
                      <span class="label">Starting Price</span>
                      <span class="value">₹
                        <fmt:formatNumber value="${item.startingPrice}" pattern="#,##0.00" />
                      </span>
                    </div>
                    <div class="price-box highlight">
                      <span class="label">Current Highest Bid</span>
                      <span class="value" id="current-price">
                        ₹
                        <fmt:formatNumber value="${item.currentPrice}" pattern="#,##0.00" />
                      </span>
                    </div>
                    <div class="price-box">
                      <span class="label">Total Bids</span>
                      <span class="value">${bidCount}</span>
                    </div>
                  </div>

                  <div class="time-box">
                    <span>Auction ends:</span>
                    <strong>
                      <fmt:formatDate value="${item.endTime}" pattern="dd MMM yyyy, HH:mm" />
                    </strong>
                  </div>
                </div>
              </div>

              <%-- Right: bid form + history --%>
                <div class="item-detail-right">

                  <c:choose>
                    <c:when test="${item.status == 'ACTIVE' and not item.ended}">
                      <div class="bid-form-box">
                        <h3>Place Your Bid</h3>
                        <form action="${pageContext.request.contextPath}/BidServlet" method="post">
                          <input type="hidden" name="itemId" value="${item.itemId}">
                          <%-- CSRF Protection: Token hidden field (generated in BidServlet.doGet) --%>
                            <input type="hidden" name="csrfToken" value="${csrfToken}">
                            <div class="form-group">
                              <label>Your Bid Amount (₹)</label>
                              <input type="number" name="bidAmount" min="${item.currentPrice + 1}" step="0.01"
                                placeholder="Enter bid higher than ₹${item.currentPrice}" required>
                            </div>
                            <button type="submit" class="btn btn-primary btn-full">Place Bid 🔨</button>
                        </form>
                      </div>
                    </c:when>
                    <c:when test="${not empty winner}">
                      <div class="winner-box">
                        <h3>🏆 Auction Ended</h3>
                        <p>Winner: <strong>${winner.winnerName}</strong></p>
                        <p>Winning Bid: <strong>₹
                            <fmt:formatNumber value="${winner.winningAmount}" pattern="#,##0.00" />
                          </strong></p>
                      </div>
                    </c:when>
                    <c:otherwise>
                      <div class="bid-form-box">
                        <p>This auction has ended with no bids.</p>
                      </div>
                    </c:otherwise>
                  </c:choose>

                  <%-- Bid History table --%>
                    <h3 class="mt-20">Bid History</h3>
                    <c:choose>
                      <c:when test="${empty bids}">
                        <p class="empty-msg">No bids yet. Be the first!</p>
                      </c:when>
                      <c:otherwise>
                        <table class="data-table">
                          <thead>
                            <tr>
                              <th>Bidder</th>
                              <th>Amount (₹)</th>
                              <th>Time</th>
                            </tr>
                          </thead>
                          <tbody>
                            <c:forEach var="bid" items="${bids}">
                              <tr class="${bid.winning ? 'row-highlight' : ''}">
                                <td>${bid.bidderName} <c:if test="${bid.winning}">👑</c:if>
                                </td>
                                <td>
                                  <fmt:formatNumber value="${bid.bidAmount}" pattern="#,##0.00" />
                                </td>
                                <td>
                                  <fmt:formatDate value="${bid.bidTime}" pattern="dd MMM HH:mm:ss" />
                                </td>
                              </tr>
                            </c:forEach>
                          </tbody>
                        </table>
                      </c:otherwise>
                    </c:choose>
                </div>

          </div><%-- .item-detail-layout --%>
        </div>

        <div id="notif-bar" class="notif-bar" style="display:none;"></div>
        <script>
          // Pass current item ID to live bid script
          const CURRENT_ITEM_ID = ${ item.itemId };
        </script>
        <script src="${pageContext.request.contextPath}/js/bid-live.js"></script>
      </body>

      </html>