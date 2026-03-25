<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <title>Admin Panel — Auction System</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<nav class="navbar">
  <div class="nav-brand">
    <img src="https://thumbs.dreamstime.com/b/online-auction-gavel-internet-bidding-web-site-win-buy-item-d-words-wood-block-closing-website-42430139.jpg" alt="AuctionHub Logo" style="height:36px; width:36px; object-fit:cover; border-radius:8px; margin-right:8px; vertical-align:middle;">
    AuctionHub &mdash; Admin
  </div>
  <div class="nav-links">
    <a href="${pageContext.request.contextPath}/LogoutServlet" class="btn btn-ghost btn-sm">Logout</a>
  </div>
</nav>

<div class="container">
  <c:if test="${not empty param.msg}">
    <div class="alert alert-success">Action completed successfully.</div>
  </c:if>

  <%-- Tabs --%>
  <div class="tab-bar">
    <button class="tab active" onclick="showTab('users', this)">Users (${allUsers.size()})</button>
    <button class="tab" onclick="showTab('auctions', this)">Auctions (${allItems.size()})</button>
    <button class="tab" onclick="showTab('winners', this)">Winners (${allWinners.size()})</button>
  </div>

  <%-- Users Table --%>
  <div id="tab-users" class="tab-content">
    <h3 class="section-title">All Users</h3>
    <table class="data-table">
      <thead><tr><th>ID</th><th>Username</th><th>Email</th><th>Role</th><th>Status</th><th>Action</th></tr></thead>
      <tbody>
        <c:forEach var="u" items="${allUsers}">
          <tr>
            <td>${u.userId}</td>
            <td>${u.username}</td>
            <td>${u.email}</td>
            <td><span class="badge ${u.role == 'ADMIN' ? 'badge-info' : 'badge-neutral'}">${u.role}</span></td>
            <td><span class="badge ${u.active ? 'badge-success' : 'badge-error'}">${u.active ? 'Active' : 'Disabled'}</span></td>
            <td>
              <%-- POST form with CSRF token — replaces old GET link (CSRF fix) --%>
              <form method="post" action="${pageContext.request.contextPath}/AdminServlet" style="display:inline;">
                <input type="hidden" name="action"    value="toggleUser">
                <input type="hidden" name="userId"    value="${u.userId}">
                <input type="hidden" name="active"    value="${!u.active}">
                <input type="hidden" name="csrfToken" value="${csrfToken}">
                <button type="submit" class="btn btn-sm ${u.active ? 'btn-outline' : 'btn-primary'}"
                        onclick="return confirm('Are you sure?')">
                  ${u.active ? 'Disable' : 'Enable'}
                </button>
              </form>
            </td>
          </tr>
        </c:forEach>
      </tbody>
    </table>
  </div>

  <%-- Auctions Table --%>
  <div id="tab-auctions" class="tab-content" style="display:none;">
    <h3 class="section-title">All Auction Items</h3>
    <table class="data-table">
      <thead><tr><th>ID</th><th>Title</th><th>Category</th><th>Current (₹)</th><th>Status</th><th>Ends</th><th>Action</th><th>Export</th></tr></thead>
      <tbody>
        <c:forEach var="item" items="${allItems}">
          <tr>
            <td>${item.itemId}</td>
            <td><a href="${pageContext.request.contextPath}/BidServlet?itemId=${item.itemId}">${item.title}</a></td>
            <td>${item.category}</td>
            <td><fmt:formatNumber value="${item.currentPrice}" pattern="#,##0.00"/></td>
            <td>
              <span class="badge
                ${item.status == 'ACTIVE' ? 'badge-success' :
                  item.status == 'CLOSED' ? 'badge-info' : 'badge-warn'}">
                ${item.status}
              </span>
            </td>
            <td><fmt:formatDate value="${item.endTime}" pattern="dd MMM HH:mm"/></td>
            <td>
              <c:if test="${item.status == 'ACTIVE'}">
                <%-- POST form with CSRF token — replaces old GET link (CSRF fix) --%>
                <form method="post" action="${pageContext.request.contextPath}/AdminServlet" style="display:inline;">
                  <input type="hidden" name="action"    value="closeAuction">
                  <input type="hidden" name="itemId"    value="${item.itemId}">
                  <input type="hidden" name="csrfToken" value="${csrfToken}">
                  <button type="submit" class="btn btn-sm btn-outline"
                          onclick="return confirm('Close this auction and declare winner?')">
                    Close
                  </button>
                </form>
              </c:if>
            </td>
            <%-- CSV Export: Kisi bhi auction ki bid history download karo --%>
            <td>
              <a href="${pageContext.request.contextPath}/AdminServlet?action=exportBidsCsv&itemId=${item.itemId}"
                 class="btn btn-sm btn-ghost"
                 title="Download bid history as CSV"
                 style="font-size:0.78rem;">📥 CSV</a>
            </td>
          </tr>
        </c:forEach>
      </tbody>
    </table>
  </div>


  <%-- Winners Table --%>
  <div id="tab-winners" class="tab-content" style="display:none;">
    <h3 class="section-title">Auction Winners</h3>
    <table class="data-table">
      <thead><tr><th>Item</th><th>Winner</th><th>Amount (₹)</th><th>Payment</th><th>Date</th><th>Action</th></tr></thead>
      <tbody>
        <c:forEach var="w" items="${allWinners}">
          <tr>
            <td>${w.itemTitle}</td>
            <td>${w.winnerName}</td>
            <td><fmt:formatNumber value="${w.winningAmount}" pattern="#,##0.00"/></td>
            <td><span class="badge ${w.paymentStatus == 'PAID' ? 'badge-success' : 'badge-warn'}">${w.paymentStatus}</span></td>
            <td><fmt:formatDate value="${w.awardedAt}" pattern="dd MMM yyyy"/></td>
            <td>
              <c:if test="${w.paymentStatus == 'PENDING'}">
                <%-- POST form with CSRF token — replaces old GET link (CSRF fix) --%>
                <form method="post" action="${pageContext.request.contextPath}/AdminServlet" style="display:inline;">
                  <input type="hidden" name="action"    value="updatePayment">
                  <input type="hidden" name="winnerId"  value="${w.winnerId}">
                  <input type="hidden" name="status"    value="PAID">
                  <input type="hidden" name="csrfToken" value="${csrfToken}">
                  <button type="submit" class="btn btn-sm btn-primary">Mark Paid</button>
                </form>
              </c:if>
            </td>
          </tr>
        </c:forEach>
      </tbody>
    </table>
  </div>

</div>

<script>
function showTab(name, btn) {
  document.querySelectorAll('.tab-content').forEach(t => t.style.display = 'none');
  document.querySelectorAll('.tab').forEach(t => t.classList.remove('active'));
  document.getElementById('tab-' + name).style.display = 'block';
  btn.classList.add('active');
}
</script>
</body>
</html>
