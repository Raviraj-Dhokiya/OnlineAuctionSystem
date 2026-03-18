<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>My Profile — AuctionHub</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>

<%-- NAVBAR --%>
<nav class="navbar">
  <div class="nav-brand">🏆 AuctionHub</div>
  <div class="nav-links">
    <a href="${pageContext.request.contextPath}/DashboardServlet" class="btn btn-ghost btn-sm">← Dashboard</a>
    <a href="${pageContext.request.contextPath}/ProfileServlet"   class="btn btn-outline btn-sm">👤 Profile</a>
    <a href="${pageContext.request.contextPath}/LogoutServlet"    class="btn btn-ghost btn-sm">Logout</a>
  </div>
</nav>

<div class="container">

  <%-- PROFILE HEADER --%>
  <div class="profile-header">
    <div class="profile-avatar">
      <span>${not empty profileUser.fullName ? profileUser.fullName.substring(0,1).toUpperCase() : profileUser.username.substring(0,1).toUpperCase()}</span>
    </div>
    <div class="profile-header-info">
      <h1 class="profile-name">${not empty profileUser.fullName ? profileUser.fullName : profileUser.username}</h1>
      <p class="profile-username">@${profileUser.username}</p>
      <span class="badge ${profileUser.admin ? 'badge-warn' : 'badge-info'}" style="margin-top:6px;">
        ${profileUser.admin ? '⭐ Admin' : '🎯 Bidder'}
      </span>
      <span class="badge badge-success" style="margin-top:6px; margin-left:6px;">
        Member since <fmt:formatDate value="${profileUser.createdAt}" pattern="MMM yyyy"/>
      </span>
    </div>
  </div>

  <%-- STATS CARDS --%>
  <div class="profile-stats">
    <div class="stat-card">
      <div class="stat-icon">🎯</div>
      <div class="stat-value">${totalBids}</div>
      <div class="stat-label">Total Bids Placed</div>
    </div>
    <div class="stat-card">
      <div class="stat-icon">🏆</div>
      <div class="stat-value">${totalWins}</div>
      <div class="stat-label">Auctions Won</div>
    </div>
    <div class="stat-card">
      <div class="stat-icon">📧</div>
      <div class="stat-value" style="font-size:1rem;">${profileUser.email}</div>
      <div class="stat-label">Email Address</div>
    </div>
    <div class="stat-card">
      <div class="stat-icon">📱</div>
      <div class="stat-value" style="font-size:1rem;">${not empty profileUser.phone ? profileUser.phone : '—'}</div>
      <div class="stat-label">Phone Number</div>
    </div>
  </div>

  <%-- TWO COLUMN LAYOUT --%>
  <div class="profile-layout">

    <%-- LEFT: EDIT PROFILE FORM --%>
    <div class="profile-form-section">
      <h2 class="section-title">✏️ Edit Profile</h2>

      <c:if test="${not empty error}">
        <div class="alert alert-error">${error}</div>
      </c:if>
      <c:if test="${not empty success}">
        <div class="alert alert-success">${success}</div>
      </c:if>

      <div class="form-card">
        <form action="${pageContext.request.contextPath}/ProfileServlet" method="post" id="profileForm">

          <div class="form-group">
            <label for="username">Username</label>
            <input type="text" id="username" value="${profileUser.username}" disabled
                   style="background:#f5f5f5; cursor:not-allowed; color:#888;">
            <small style="color:#999; font-size:0.8rem;">Username change nahi ho sakta</small>
          </div>

          <div class="form-group">
            <label for="fullName">Full Name *</label>
            <input type="text" id="fullName" name="fullName"
                   value="${profileUser.fullName}" placeholder="Apna pura naam likhein" required>
          </div>

          <div class="form-group">
            <label for="emailDisp">Email</label>
            <input type="email" id="emailDisp" value="${profileUser.email}" disabled
                   style="background:#f5f5f5; cursor:not-allowed; color:#888;">
            <small style="color:#999; font-size:0.8rem;">Email change ke liye admin se contact karein</small>
          </div>

          <div class="form-group">
            <label for="phone">Phone Number</label>
            <input type="tel" id="phone" name="phone"
                   value="${profileUser.phone}" placeholder="e.g. 9876543210">
          </div>

          <hr style="margin: 20px 0; border-color:#f0f0f0;">
          <h3 style="margin-bottom:12px; font-size:1rem; color:#444;">🔒 Password Change (Optional)</h3>
          <small style="color:#999; display:block; margin-bottom:14px;">
            Sirf tab bharein jab password change karna ho
          </small>

          <div class="form-group">
            <label for="newPassword">New Password</label>
            <input type="password" id="newPassword" name="newPassword"
                   placeholder="8+ chars, uppercase, digit, special char">
          </div>

          <div class="form-group">
            <label for="confirmPassword">Confirm New Password</label>
            <input type="password" id="confirmPassword" name="confirmPassword"
                   placeholder="Dobara wahi password likhein">
          </div>

          <button type="submit" class="btn btn-primary btn-full" style="margin-top:8px;">
            💾 Save Changes
          </button>
        </form>
      </div>
    </div>

    <%-- RIGHT: BID HISTORY --%>
    <div class="profile-activity-section">
      <h2 class="section-title">📋 My Bid History</h2>
      <div class="table-wrap">
        <c:choose>
          <c:when test="${empty myBids}">
            <div class="empty-state-mini">
              <span>🎯</span>
              <p>Abhi tak koi bid nahi lagayi.</p>
              <a href="${pageContext.request.contextPath}/DashboardServlet" class="btn btn-outline btn-sm">Browse Auctions</a>
            </div>
          </c:when>
          <c:otherwise>
            <table class="data-table">
              <thead>
                <tr>
                  <th>Item</th>
                  <th>Bid Amount (₹)</th>
                  <th>Date & Time</th>
                  <th>Status</th>
                </tr>
              </thead>
              <tbody>
                <c:forEach var="bid" items="${myBids}">
                  <tr>
                    <td>
                      <a href="${pageContext.request.contextPath}/BidServlet?itemId=${bid.itemId}">
                        ${bid.itemTitle}
                      </a>
                    </td>
                    <td><strong>₹<fmt:formatNumber value="${bid.bidAmount}" pattern="#,##0.00"/></strong></td>
                    <td><fmt:formatDate value="${bid.bidTime}" pattern="dd MMM yyyy HH:mm"/></td>
                    <td>
                      <c:choose>
                        <c:when test="${bid.winning}">
                          <span class="badge badge-success">🏆 Winning</span>
                        </c:when>
                        <c:otherwise>
                          <span class="badge badge-neutral">📌 Placed</span>
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

      <%-- WINS SECTION --%>
      <h2 class="section-title" style="margin-top:32px;">🏆 Auctions Won</h2>
      <div class="table-wrap">
        <c:choose>
          <c:when test="${empty myWins}">
            <div class="empty-state-mini">
              <span>🏆</span>
              <p>Abhi tak koi auction nahi jeeta. Bidding karo!</p>
            </div>
          </c:when>
          <c:otherwise>
            <table class="data-table">
              <thead>
                <tr>
                  <th>Item</th>
                  <th>Winning Bid (₹)</th>
                  <th>Won on</th>
                  <th>Payment</th>
                </tr>
              </thead>
              <tbody>
                <c:forEach var="win" items="${myWins}">
                  <tr class="row-highlight">
                    <td><strong>${win.itemTitle}</strong></td>
                    <td><strong style="color:#6c63ff;">₹<fmt:formatNumber value="${win.winningAmount}" pattern="#,##0.00"/></strong></td>
                    <td><fmt:formatDate value="${win.awardedAt}" pattern="dd MMM yyyy"/></td>
                    <td>
                      <c:choose>
                        <c:when test="${win.paymentStatus == 'PAID'}">
                          <span class="badge badge-success">✅ Paid</span>
                        </c:when>
                        <c:otherwise>
                          <span class="badge badge-warn">⏳ Pending</span>
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
    </div>

  </div><%-- .profile-layout --%>

</div><%-- .container --%>

<script>
// Password match validation
document.getElementById('profileForm').addEventListener('submit', function(e) {
  var np = document.getElementById('newPassword').value;
  var cp = document.getElementById('confirmPassword').value;
  if (np && np !== cp) {
    e.preventDefault();
    alert('Passwords match nahi karte! Please dobara check karein.');
  }
});
</script>

</body>
</html>
