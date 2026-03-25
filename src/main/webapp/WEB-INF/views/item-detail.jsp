<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="en">

<head>
  <meta charset="UTF-8">
  <title>${item.title} — Auction System</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
  <style>
    /* ── Chat UI Styles ─────────────────────────────────────────────────── */
    .chat-section {
      margin-top: 2rem;
      background: #fff;
      border: 1px solid #e0e0e0;
      border-radius: 14px;
      overflow: hidden;
      box-shadow: 0 2px 10px rgba(0,0,0,0.07);
    }
    .chat-header {
      background: #1a1a2e;
      padding: 0.9rem 1.2rem;
      display: flex;
      align-items: center;
      gap: 0.6rem;
      font-weight: 600;
      font-size: 0.95rem;
      color: #fff;
    }
    .chat-status-dot {
      width: 8px; height: 8px;
      border-radius: 50%;
      background: #22c55e;
      box-shadow: 0 0 6px #22c55e;
      animation: pulse-dot 2s infinite;
    }
    @keyframes pulse-dot {
      0%,100% { opacity:1; } 50% { opacity:0.4; }
    }
    .chat-messages {
      height: 280px;
      overflow-y: auto;
      padding: 1rem;
      display: flex;
      flex-direction: column;
      gap: 0.5rem;
      background: #f8f9fb;
    }
    .chat-msg {
      display: flex;
      gap: 0.6rem;
      align-items: flex-start;
      animation: fadeInMsg 0.3s ease;
    }
    @keyframes fadeInMsg {
      from { opacity:0; transform:translateY(6px); }
      to   { opacity:1; transform:translateY(0); }
    }
    .chat-msg .avatar {
      width: 30px; height: 30px;
      border-radius: 50%;
      background: linear-gradient(135deg, #6c63ff, #8b5cf6);
      display: flex; align-items: center; justify-content: center;
      font-size: 0.75rem; font-weight: 700;
      color: white; flex-shrink: 0;
    }
    .chat-msg .bubble {
      background: #e8e8f0;
      border-radius: 10px 10px 10px 2px;
      padding: 0.45rem 0.75rem;
      max-width: 85%;
    }
    .chat-msg.mine .bubble {
      background: #f0eeff;
      border: 1px solid #c5bfff;
      border-radius: 10px 10px 2px 10px;
      margin-left: auto;
    }
    .chat-msg.mine { flex-direction: row-reverse; }
    .chat-msg .sender {
      font-size: 0.72rem;
      font-weight: 600;
      color: #6c63ff;
      margin-bottom: 2px;
    }
    .chat-msg.mine .sender { color: #574fd6; text-align: right; }
    .chat-msg .text {
      font-size: 0.85rem;
      color: #1a1a2e;
      word-break: break-word;
    }
    .chat-msg .ts {
      font-size: 0.68rem;
      color: #999;
      margin-top: 2px;
      text-align: right;
    }
    .chat-msg.system-msg {
      justify-content: center;
    }
    .chat-msg.system-msg .bubble {
      background: transparent;
      font-size: 0.75rem;
      color: #888;
      font-style: italic;
      text-align: center;
      padding: 0.2rem 0.5rem;
    }
    .chat-input-row {
      display: flex;
      gap: 0.5rem;
      padding: 0.75rem 1rem;
      background: #f0f0f7;
      border-top: 1px solid #e0e0e0;
    }
    .chat-input-row input {
      flex: 1;
      background: #fff;
      border: 1.5px solid #ddd;
      border-radius: 8px;
      padding: 0.5rem 0.8rem;
      color: #1a1a2e;
      font-size: 0.88rem;
      outline: none;
      transition: border-color 0.2s;
    }
    .chat-input-row input::placeholder { color: #aaa; }
    .chat-input-row input:focus {
      border-color: #6c63ff;
      background: #fff;
    }
    .chat-input-row input:disabled {
      background: #f0f0f0;
      color: #aaa;
    }
    .chat-input-row button {
      padding: 0.5rem 1.1rem;
      background: #6c63ff;
      border: none;
      border-radius: 8px;
      color: white;
      font-size: 0.88rem;
      font-weight: 600;
      cursor: pointer;
      transition: background 0.2s;
    }
    .chat-input-row button:hover:not(:disabled) { background: #574fd6; }
    .chat-input-row button:disabled { background: #ccc; cursor: not-allowed; }
    .chat-reconnect-bar {
      background: #ffe6e6;
      color: #c0392b;
      border-top: 1px solid #f5c6c6;
      font-size: 0.78rem;
      text-align: center;
      padding: 0.4rem;
      display: none;
    }

    /* ── Watchlist Button ───────────────────────────────────────────────── */
    .watchlist-btn {
      display: inline-flex;
      align-items: center;
      gap: 0.4rem;
      padding: 0.5rem 1rem;
      border-radius: 8px;
      font-size: 0.88rem;
      font-weight: 600;
      cursor: pointer;
      text-decoration: none;
      transition: all 0.2s;
      margin-top: 0.8rem;
    }
    .watchlist-btn.watching {
      background: #fff8e1;
      color: #856404;
      border: 1.5px solid #f0c040;
    }
    .watchlist-btn.watching:hover {
      background: #fff3cd;
    }
    .watchlist-btn.not-watching {
      background: #f0eeff;
      color: #6c63ff;
      border: 1.5px solid #c5bfff;
    }
    .watchlist-btn.not-watching:hover {
      background: #e8e4ff;
    }

    /* ── Min Bid Suggestion ─────────────────────────────────────────────── */
    .min-bid-hint {
      background: #eef3ff;
      border: 1.5px solid #b0c4ff;
      border-radius: 8px;
      padding: 0.65rem 1rem;
      margin-bottom: 0.8rem;
      font-size: 0.87rem;
      color: #2d46b9;
      display: flex;
      align-items: center;
      gap: 0.5rem;
    }
    .min-bid-hint strong { color: #1a32a0; font-size: 1rem; }

    /* ── Time Extension Notice ──────────────────────────────────────────── */
    .extension-notice {
      background: #fff8e1;
      border: 1.5px solid #ffe082;
      border-radius: 8px;
      padding: 0.6rem 0.9rem;
      font-size: 0.82rem;
      color: #856404;
      margin-top: 0.8rem;
      line-height: 1.5;
    }

    /* ══════════════════════════════════════════════════════════════
       AUTO BID STYLES
    ══════════════════════════════════════════════════════════════ */
    .auto-bid-section {
      margin-top: 1rem;
      border-top: 1.5px dashed #d0c9ff;
      padding-top: 1rem;
    }
    /* Active status banner */
    .auto-bid-status-bar {
      display: flex;
      align-items: center;
      gap: 0.6rem;
      background: linear-gradient(135deg, #f0eeff 0%, #e8e4ff 100%);
      border: 1.5px solid #b4a9ff;
      border-radius: 10px;
      padding: 0.7rem 1rem;
      margin-bottom: 0.8rem;
      font-size: 0.85rem;
      color: #3b2fc9;
      font-weight: 600;
      animation: auto-bid-pulse 2.5s infinite;
    }
    @keyframes auto-bid-pulse {
      0%,100% { box-shadow: 0 0 0 0 rgba(108,99,255,0.25); }
      50%      { box-shadow: 0 0 0 6px rgba(108,99,255,0); }
    }
    .auto-bid-dot {
      width: 10px; height: 10px;
      border-radius: 50%;
      background: #6c63ff;
      animation: pulse-dot 1.5s infinite;
      flex-shrink: 0;
    }
    /* Buttons */
    .btn-auto-bid-enable {
      display: inline-flex;
      align-items: center;
      gap: 0.45rem;
      padding: 0.6rem 1.2rem;
      border-radius: 10px;
      font-size: 0.9rem;
      font-weight: 700;
      cursor: pointer;
      border: none;
      background: linear-gradient(135deg, #6c63ff 0%, #8b5cf6 100%);
      color: #fff;
      box-shadow: 0 4px 12px rgba(108,99,255,0.35);
      transition: transform 0.15s, box-shadow 0.15s;
      width: 100%;
      justify-content: center;
    }
    .btn-auto-bid-enable:hover {
      transform: translateY(-2px);
      box-shadow: 0 6px 18px rgba(108,99,255,0.45);
    }
    .btn-auto-bid-cancel {
      display: inline-flex;
      align-items: center;
      gap: 0.4rem;
      padding: 0.45rem 1rem;
      border-radius: 8px;
      font-size: 0.82rem;
      font-weight: 600;
      cursor: pointer;
      border: 1.5px solid #f87171;
      background: #fff5f5;
      color: #c0392b;
      transition: all 0.2s;
      margin-top: 0.4rem;
      width: 100%;
      justify-content: center;
    }
    .btn-auto-bid-cancel:hover { background: #fee2e2; }
    /* Modal overlay */
    .auto-bid-overlay {
      display: none;
      position: fixed;
      inset: 0;
      background: rgba(10,10,30,0.55);
      backdrop-filter: blur(4px);
      z-index: 9000;
      align-items: center;
      justify-content: center;
    }
    .auto-bid-overlay.open { display: flex; }
    .auto-bid-modal {
      background: #fff;
      border-radius: 16px;
      box-shadow: 0 20px 60px rgba(0,0,0,0.25);
      padding: 2rem;
      width: 100%;
      max-width: 420px;
      animation: modal-in 0.25s ease;
    }
    @keyframes modal-in {
      from { opacity:0; transform: scale(0.9) translateY(20px); }
      to   { opacity:1; transform: scale(1) translateY(0); }
    }
    .auto-bid-modal h3 {
      margin: 0 0 0.3rem 0;
      font-size: 1.2rem;
      color: #1a1a2e;
      display: flex;
      align-items: center;
      gap: 0.5rem;
    }
    .auto-bid-modal .modal-sub {
      font-size: 0.82rem;
      color: #64748b;
      margin-bottom: 1.2rem;
    }
    .auto-bid-modal .info-row {
      background: #f0eeff;
      border: 1px solid #c5bfff;
      border-radius: 8px;
      padding: 0.6rem 0.9rem;
      font-size: 0.84rem;
      color: #3b2fc9;
      margin-bottom: 1rem;
      line-height: 1.6;
    }
    .auto-bid-modal label {
      display: block;
      font-size: 0.85rem;
      font-weight: 600;
      color: #374151;
      margin-bottom: 0.3rem;
    }
    .auto-bid-modal input[type=number] {
      width: 100%;
      padding: 0.6rem 0.8rem;
      border: 1.5px solid #d1d5db;
      border-radius: 8px;
      font-size: 1rem;
      color: #1a1a2e;
      outline: none;
      transition: border-color 0.2s;
      box-sizing: border-box;
    }
    .auto-bid-modal input[type=number]:focus { border-color: #6c63ff; }
    .modal-footer {
      display: flex;
      gap: 0.7rem;
      margin-top: 1.2rem;
    }
    .modal-footer .btn-confirm {
      flex: 1;
      padding: 0.65rem;
      border-radius: 9px;
      border: none;
      background: linear-gradient(135deg,#6c63ff,#8b5cf6);
      color: #fff;
      font-weight: 700;
      font-size: 0.95rem;
      cursor: pointer;
      transition: opacity 0.2s;
    }
    .modal-footer .btn-confirm:hover { opacity:0.88; }
    .modal-footer .btn-close-modal {
      padding: 0.65rem 1.1rem;
      border-radius: 9px;
      border: 1.5px solid #d1d5db;
      background: #f9fafb;
      color: #4b5563;
      font-weight: 600;
      font-size: 0.9rem;
      cursor: pointer;
      transition: background 0.2s;
    }
    .modal-footer .btn-close-modal:hover { background: #e5e7eb; }
    /* Timer countdown badge */
    .auto-bid-timer {
      display: none;
      align-items: center;
      gap: 0.5rem;
      background: #fff7ed;
      border: 1.5px solid #fdba74;
      border-radius: 8px;
      padding: 0.5rem 0.8rem;
      font-size: 0.83rem;
      color: #92400e;
      font-weight: 600;
      margin-top: 0.5rem;
    }
    .auto-bid-timer.visible { display: flex; }
    .auto-bid-toast {
      position: fixed;
      bottom: 1.5rem;
      right: 1.5rem;
      background: #1a1a2e;
      color: #fff;
      padding: 0.8rem 1.2rem;
      border-radius: 10px;
      font-size: 0.88rem;
      font-weight: 600;
      z-index: 9999;
      opacity: 0;
      transform: translateY(20px);
      transition: opacity 0.3s, transform 0.3s;
      max-width: 320px;
    }
    .auto-bid-toast.show {
      opacity: 1;
      transform: translateY(0);
    }
    .auto-bid-toast.success { border-left: 4px solid #22c55e; }
    .auto-bid-toast.error   { border-left: 4px solid #ef4444; }
    .auto-bid-toast.info    { border-left: 4px solid #6c63ff; }
  </style>
</head>

<body>

  <nav class="navbar">
    <div class="nav-brand">
      <img src="https://thumbs.dreamstime.com/b/online-auction-gavel-internet-bidding-web-site-win-buy-item-d-words-wood-block-closing-website-42430139.jpg" alt="AuctionHub Logo" style="height:36px; width:36px; object-fit:cover; border-radius:8px; margin-right:8px; vertical-align:middle;">
      AuctionHub
    </div>
    <div class="nav-links">
      <a href="${pageContext.request.contextPath}/DashboardServlet"   class="btn btn-ghost btn-sm">← Back</a>
      <a href="${pageContext.request.contextPath}/WatchlistServlet"   class="btn btn-ghost btn-sm">⭐ Watchlist</a>
      <span>Welcome, <strong>${sessionScope.username}</strong></span>
      <a href="${pageContext.request.contextPath}/LogoutServlet"      class="btn btn-ghost btn-sm">Logout</a>
    </div>
  </nav>

  <div class="container">

    <%-- Alerts --%>
    <c:if test="${param.success == '1'}">
      <div class="alert alert-success">✓ Your bid was placed successfully!</div>
    </c:if>
    <c:if test="${param.error == 'bid_low'}">
      <div class="alert alert-error">Your bid must be higher than the current bid.</div>
    </c:if>
    <c:if test="${param.error == 'own_item'}">
      <div class="alert alert-error">You cannot bid on your own product.</div>
    </c:if>
    <c:if test="${param.watchAdded == '1'}">
      <div class="alert alert-success">⭐ Item added to your watchlist!</div>
    </c:if>
    <c:if test="${param.watchRemoved == '1'}">
      <div class="alert alert-success">Removed from watchlist.</div>
    </c:if>

    <div class="item-detail-layout">

      <%-- Left: image + details --%>
      <div class="item-detail-left">
        <img src="${pageContext.request.contextPath}/AuctionItemServlet?action=image&itemId=${item.itemId}"
             alt="${item.title}" class="item-detail-img" id="mainImage"
             onerror="this.style.display='none'; document.getElementById('mainImgPlaceholder').style.display='flex';"
             onload="document.getElementById('mainImgPlaceholder').style.display='none'; this.style.display='';"
        >
        <div id="mainImgPlaceholder" class="item-detail-img-placeholder" style="display:none;">📦</div>

        <%-- MULTI-IMAGE GALLERY (Added) --%>
        <div class="item-gallery" style="display:flex; gap:10px; margin-top:15px; margin-bottom:20px; overflow-x:auto;" id="galleryStrip">
          <!-- Thumbnail of main cover image -->
          <img src="${pageContext.request.contextPath}/AuctionItemServlet?action=image&itemId=${item.itemId}"
               style="height:70px; width:70px; border-radius:8px; object-fit:cover; border:2px solid #ddd; cursor:pointer;"
               onerror="this.style.display='none';"
               onclick="document.getElementById('mainImage').src=this.src; document.getElementById('mainImgPlaceholder').style.display='none'; document.getElementById('mainImage').style.display='';"
          >
          <!-- Extra 4 Thumbnails -->
          <c:forEach var="imgId" items="${extraImages}">
            <img src="${pageContext.request.contextPath}/AuctionItemServlet?action=specificImage&itemId=${item.itemId}&imgId=${imgId}"
                 style="height:70px; width:70px; border-radius:8px; object-fit:cover; border:2px solid #ddd; cursor:pointer;"
                 onerror="this.style.display='none';"
                 onclick="document.getElementById('mainImage').src=this.src; document.getElementById('mainImgPlaceholder').style.display='none'; document.getElementById('mainImage').style.display='';"
            >
          </c:forEach>
        </div>

        <div class="item-meta">
          <span class="category-badge">${item.category}</span>
          <h1>${item.title}</h1>
          <p class="seller-info">Listed by: <strong>${item.sellerName}</strong></p>
          <p class="item-desc-full">${item.description}</p>

          <%-- Watchlist Button --%>
          <c:choose>
            <c:when test="${isWatching}">
              <a href="${pageContext.request.contextPath}/WatchlistServlet?action=remove&itemId=${item.itemId}&ref=item"
                 class="watchlist-btn watching">
                ⭐ Watching — Click to Remove
              </a>
            </c:when>
            <c:otherwise>
              <a href="${pageContext.request.contextPath}/WatchlistServlet?action=add&itemId=${item.itemId}"
                 class="watchlist-btn not-watching">
                ☆ Add to Watchlist
              </a>
            </c:otherwise>
          </c:choose>

          <div class="price-section">
            <div class="price-box">
              <span class="label">Starting Price</span>
              <span class="value">₹<fmt:formatNumber value="${item.startingPrice}" pattern="#,##0.00"/></span>
            </div>
            <div class="price-box highlight">
              <span class="label">Current Highest Bid</span>
              <span class="value" id="current-price">
                ₹<fmt:formatNumber value="${item.currentPrice}" pattern="#,##0.00"/>
              </span>
            </div>
            <div class="price-box">
              <span class="label">Total Bids</span>
              <span class="value" id="total-bids">${bidCount}</span>
            </div>
          </div>

          <div class="time-box">
            <span>Auction ends:</span>
            <strong>
              <fmt:formatDate value="${item.endTime}" pattern="dd MMM yyyy, HH:mm"/>
            </strong>
          </div>

          <%-- Time Extension Notice --%>
          <c:if test="${item.status == 'ACTIVE'}">
            <div class="extension-notice">
              ⏱️ <strong>Auto-Extension Rule:</strong> If a bid is placed in the last 2 minutes,
              the auction automatically extends by <strong>5 minutes</strong>.
            </div>
          </c:if>
        </div>
      </div>

      <%-- Right: bid form + history + chat --%>
      <div class="item-detail-right">

        <c:choose>
          <c:when test="${item.status == 'ACTIVE' and not item.ended}">
            <div class="bid-form-box">
              <h3>Place Your Bid</h3>

              <%-- Minimum Bid Suggestion --%>
              <c:if test="${not empty minNextBid}">
                <div class="min-bid-hint">
                  💡 Minimum next bid:
                  <strong>₹<fmt:formatNumber value="${minNextBid}" pattern="#,##0.00"/></strong>
                  &nbsp;(current + 5%)
                </div>
              </c:if>

              <form action="${pageContext.request.contextPath}/BidServlet" method="post">
                <input type="hidden" name="itemId"    value="${item.itemId}">
                <input type="hidden" name="csrfToken" value="${csrfToken}">
                <div class="form-group">
                  <label>Your Bid Amount (₹)</label>
                  <input type="number" name="bidAmount"
                         min="${minNextBid}"
                         step="1"
                         placeholder="Min: ₹${minNextBid}" required
                         id="bidAmountInput">
                </div>
                <button type="submit" class="btn btn-primary btn-full">Place Bid 🔨</button>
              </form>

              <%-- ═══════════════════════ AUTO BID SECTION ═══════════════════════ --%>
              <div class="auto-bid-section" id="auto-bid-section">

                <%-- Active Status Bar (hidden by default, JS se dikhega) --%>
                <div class="auto-bid-status-bar" id="auto-bid-status-bar" style="display:none;">
                  <div class="auto-bid-dot"></div>
                  🤖 Auto Bid Active &mdash; Target: <span id="ab-target-display">&#8377;0</span>
                </div>

                <%-- Countdown timer (30 sec) --%>
                <div class="auto-bid-timer" id="auto-bid-timer">
                  ⏳ Auto bid in <strong><span id="ab-countdown">30</span>s</strong>...
                </div>

                <%-- Enable Button --%>
                <button class="btn-auto-bid-enable" id="btn-enable-auto-bid"
                        onclick="openAutoBidModal()" style="display:inline-flex;">
                  🤖 Enable Auto Bid
                </button>

                <%-- Cancel Button --%>
                <button class="btn-auto-bid-cancel" id="btn-cancel-auto-bid"
                        style="display:none;" onclick="cancelAutoBid()">
                  ✕ Disable Auto Bid
                </button>

              </div><%-- /auto-bid-section --%>
            </div>
          </c:when>
          <c:when test="${not empty winner}">
            <div class="winner-box">
              <h3>🏆 Auction Ended</h3>
              <p>Winner: <strong>${winner.winnerName}</strong></p>
              <p>Winning Bid: <strong>₹<fmt:formatNumber value="${winner.winningAmount}" pattern="#,##0.00"/></strong></p>
            </div>
          </c:when>
          <c:otherwise>
            <div class="bid-form-box">
              <p>This auction has ended with no bids.</p>
            </div>
          </c:otherwise>
        </c:choose>

        <%-- Bid History table --%>
        <div style="display:flex; justify-content:space-between; align-items:center; margin-top:20px; margin-bottom:10px;">
          <h3 style="margin:0;">Bid History</h3>
          <a href="${pageContext.request.contextPath}/DownloadBidsPdfServlet?itemId=${item.itemId}" 
             class="btn btn-outline btn-sm">📄 Download PDF</a>
        </div>
        <p class="empty-msg" id="empty-bids-msg" style="${empty bids ? 'display:block;' : 'display:none;'}">No bids yet. Be the first!</p>
        
        <table class="data-table" id="bid-table" style="${empty bids ? 'display:none;' : ''}">
          <thead>
            <tr>
              <th>Bidder</th>
              <th>Amount (₹)</th>
              <th>Time</th>
            </tr>
          </thead>
          <tbody id="bid-history-body">
            <c:forEach var="bid" items="${bids}">
              <tr class="${bid.winning ? 'row-highlight' : ''}">
                <td>${bid.bidderName} <c:if test="${bid.winning}">👑</c:if></td>
                <td><fmt:formatNumber value="${bid.bidAmount}" pattern="#,##0.00"/></td>
                <td><fmt:formatDate value="${bid.bidTime}" pattern="dd MMM HH:mm:ss"/></td>
              </tr>
            </c:forEach>
          </tbody>
        </table>

        <%-- ═══════════════════════════════════════════════════════
             LIVE CHAT (Socket-based — AuctionChatServer port 9092)
             ═══════════════════════════════════════════════════════ --%>
        <div class="chat-section">
          <div class="chat-header">
            <div class="chat-status-dot" id="chat-dot"></div>
            💬 Live Auction Chat
            <span id="chat-status-label" style="font-size:0.75rem; color:#64748b; margin-left:auto;">Connecting...</span>
          </div>
          <div id="chat-reconnect-bar" class="chat-reconnect-bar">
            ⚠️ Chat disconnected. Reconnecting...
          </div>
          <div class="chat-messages" id="chat-messages">
            <div class="chat-msg system-msg">
              <div class="bubble">Loading chat history...</div>
            </div>
          </div>
          <div class="chat-input-row">
            <input type="text" id="chat-input" placeholder="Type a message..." maxlength="400"
                   autocomplete="off" disabled>
            <button onclick="sendChat()" id="chat-send-btn" disabled>Send</button>
          </div>
        </div>

      </div>
    </div><%-- .item-detail-layout --%>
  </div>

  <div id="notif-bar" class="notif-bar" style="display:none;"></div>

  <%-- ══════════════════════════════════════════════════════════
       AUTO BID MODAL
  ══════════════════════════════════════════════════════════ --%>
  <div class="auto-bid-overlay" id="auto-bid-overlay" onclick="overlayClickClose(event)">
    <div class="auto-bid-modal">
      <h3>🤖 Auto Bid Setup</h3>
      <p class="modal-sub">Auto bid fires automatically 30 seconds after someone else outbids you.</p>

      <div class="info-row">
        📌 <strong>How it works:</strong><br>
        &bull; Auto bid triggers <strong>30 seconds</strong> after another user's bid<br>
        &bull; Bid amount = Current Price + <strong>10%</strong><br>
        &bull; If auto bid amount exceeds your target, auto bid <strong>stops</strong> automatically
      </div>

      <div id="ab-current-info" style="font-size:0.85rem; color:#374151; margin-bottom:0.8rem;">
        Current Highest Bid: <strong id="ab-modal-current-price">&#8377;0</strong>
      </div>

      <label for="ab-max-target-input">🎯 Maximum Target Price (&#8377;)</label>
      <input type="number" id="ab-max-target-input"
             placeholder="e.g. 10000" min="1" step="1">
      <div style="font-size:0.75rem; color:#94a3b8; margin-top:0.3rem;">
        Auto bid will stop once this price limit is reached.
      </div>

      <div class="modal-footer">
        <button class="btn-confirm" onclick="confirmAutoBid()">&#10003; Activate Auto Bid</button>
        <button class="btn-close-modal" onclick="closeAutoBidModal()">Cancel</button>
      </div>
    </div>
  </div>

  <%-- Auto Bid Toast Notification --%>
  <div class="auto-bid-toast" id="auto-bid-toast"></div>

  <script>
    // --- Item & User Context ---
    const CURRENT_ITEM_ID = parseInt("${item.itemId}", 10);
    const MY_USERNAME     = "${sessionScope.username}";
    const CTX             = "${pageContext.request.contextPath}";

    let lastMsgId = 0;
    let pollTimer = null;
    let chatReady = false;

    const chatDot   = document.getElementById('chat-dot');
    const chatLabel = document.getElementById('chat-status-label');
    const chatInput = document.getElementById('chat-input');
    const chatSend  = document.getElementById('chat-send-btn');
    const chatBox   = document.getElementById('chat-messages');
    const reconnBar = document.getElementById('chat-reconnect-bar');

    function setChatOnline() {
      chatDot.style.background = '#22c55e';
      chatDot.style.boxShadow  = '0 0 6px #22c55e';
      chatLabel.textContent    = 'Connected';
      chatLabel.style.color    = '#6ee7b7';
      chatInput.disabled       = false;
      chatSend.disabled        = false;
      reconnBar.style.display  = 'none';
      chatReady = true;
    }

    function setChatOffline() {
      chatDot.style.background = '#ef4444';
      chatDot.style.boxShadow  = '0 0 6px #ef4444';
      chatLabel.textContent    = 'Offline';
      chatLabel.style.color    = '#fca5a5';
      chatInput.disabled       = true;
      chatSend.disabled        = true;
      reconnBar.style.display  = 'block';
      chatReady = false;
    }

    // Render a single message bubble into the chat box
    function appendMsg(sender, text, timestamp, isSystem) {
      const div = document.createElement('div');
      if (isSystem) {
        div.className = 'chat-msg system-msg';
        div.innerHTML = '<div class="bubble">' + escHtml(text) + '</div>';
      } else {
        const isMine  = (sender === MY_USERNAME);
        div.className = 'chat-msg' + (isMine ? ' mine' : '');
        const initial = sender ? sender.charAt(0).toUpperCase() : '?';
        const ts = timestamp
          ? new Date(timestamp).toLocaleTimeString([], {hour:'2-digit', minute:'2-digit'})
          : '';
        div.innerHTML =
          '<div class="avatar">' + escHtml(initial) + '</div>' +
          '<div class="bubble">' +
            '<div class="sender">' + escHtml(sender) + '</div>' +
            '<div class="text">'   + escHtml(text)   + '</div>' +
            '<div class="ts">'     + ts              + '</div>' +
          '</div>';
      }
      // Remove "Loading..." placeholder on first real message
      const loading = chatBox.querySelector('.system-msg');
      if (loading && loading.querySelector('.bubble') &&
          loading.querySelector('.bubble').textContent.trim() === 'Loading chat history...') {
        loading.remove();
      }
      chatBox.appendChild(div);
      chatBox.scrollTop = chatBox.scrollHeight;
    }

    function escHtml(str) {
      if (!str) return '';
      return str.replace(/&/g,'&amp;').replace(/</g,'&lt;')
                .replace(/>/g,'&gt;').replace(/"/g,'&quot;');
    }

    // Poll DB for new messages every 2 seconds
    function pollMessages() {
      fetch(CTX + '/ChatPollServlet?itemId=' + CURRENT_ITEM_ID + '&since=' + lastMsgId)
        .then(function(r) { return r.json(); })
        .then(function(data) {
          if (!chatReady) setChatOnline();
          if (data && data.messages) {
            data.messages.forEach(function(m) {
              appendMsg(m.sender, m.content, m.sentAt, false);
              if (m.msgId > lastMsgId) lastMsgId = m.msgId;
            });
          }
        })
        .catch(function() { setChatOffline(); });
    }

    // Send message via POST
    function sendChat() {
      var msg = chatInput.value.trim();
      if (!msg || !chatReady) return;
      chatInput.value = '';

      fetch(CTX + '/ChatSendServlet', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: 'itemId=' + CURRENT_ITEM_ID + '&content=' + encodeURIComponent(msg)
      })
      .then(function(r) { return r.json(); })
      .then(function(d) {
        if (d.ok) {
          appendMsg(MY_USERNAME, msg, new Date().toISOString(), false);
        }
      })
      .catch(function() { setChatOffline(); });
    }

    // Enter key to send
    chatInput.addEventListener('keydown', function(e) {
      if (e.key === 'Enter') sendChat();
    });

    // Start polling
    pollMessages();
    pollTimer = setInterval(pollMessages, 2000);
    window.addEventListener('beforeunload', function() { clearInterval(pollTimer); });
  </script>

  <%-- itemId global — required by bid-live.js for poll filtering + auto bid hook --%>
  <script>
    var itemId = parseInt('${item.itemId}', 10);
  </script>

  <script src="${pageContext.request.contextPath}/js/bid-live.js"></script>

  <script>
    // ── AUTO BID JAVASCRIPT ───────────────────────────────────────────────────

    var AB_ITEM_ID   = parseInt('${item.itemId}', 10);
    var AB_CTX       = '${pageContext.request.contextPath}';
    var AB_MY_USER   = '${sessionScope.username}';

    var abActive         = false;
    var abMaxTarget      = 0;
    var abCountdownTimer = null;
    var abCountdownSec   = 30;
    var abLastBidder     = '';
    var abProcessing     = false;
    var abToastTimer     = null;

    // ── On page load: check if auto bid is already active ────────────────────
    window.addEventListener('load', function() {
      fetch(AB_CTX + '/AutoBidServlet?itemId=' + AB_ITEM_ID)
        .then(function(r) { return r.json(); })
        .then(function(data) {
          if (data.active) {
            abActive    = true;
            abMaxTarget = data.maxTarget;
            showAutoBidActive();
          }
        })
        .catch(function() {});
    });


    // ── Show auto bid ACTIVE state ────────────────────────────────────────────
    function showAutoBidActive() {
      document.getElementById('auto-bid-status-bar').style.display = 'flex';
      document.getElementById('ab-target-display').textContent =
        String.fromCharCode(8377) + Number(abMaxTarget).toLocaleString('en-IN');
      document.getElementById('btn-enable-auto-bid').style.display = 'none';
      document.getElementById('btn-cancel-auto-bid').style.display = 'inline-flex';
    }

    // ── Show auto bid INACTIVE state ──────────────────────────────────────────
    function showAutoBidInactive() {
      abActive = false;
      document.getElementById('auto-bid-status-bar').style.display = 'none';
      document.getElementById('btn-enable-auto-bid').style.display = 'inline-flex';
      document.getElementById('btn-cancel-auto-bid').style.display = 'none';
      stopAutoBidCountdown();
    }

    // ── MODAL: open ───────────────────────────────────────────────────────────
    function openAutoBidModal() {
      var priceEl   = document.getElementById('current-price');
      var priceText = priceEl ? priceEl.textContent.trim() : String.fromCharCode(8377) + '0';
      document.getElementById('ab-modal-current-price').textContent = priceText;
      document.getElementById('auto-bid-overlay').classList.add('open');
      setTimeout(function() {
        document.getElementById('ab-max-target-input').focus();
      }, 200);
    }

    // ── MODAL: close ──────────────────────────────────────────────────────────
    function closeAutoBidModal() {
      document.getElementById('auto-bid-overlay').classList.remove('open');
      document.getElementById('ab-max-target-input').value = '';
    }

    function overlayClickClose(e) {
      if (e.target === document.getElementById('auto-bid-overlay')) {
        closeAutoBidModal();
      }
    }

    // ── CONFIRM: Enable Auto Bid ──────────────────────────────────────────────
    function confirmAutoBid() {
      var inp       = document.getElementById('ab-max-target-input');
      var targetVal = parseFloat(inp.value);

      if (!targetVal || isNaN(targetVal) || targetVal <= 0) {
        showAutoBidToast('Please enter a valid target price.', 'error');
        inp.focus();
        return;
      }

      // Disable button while saving
      var btn = document.querySelector('.btn-confirm');
      if (btn) { btn.disabled = true; btn.textContent = 'Saving...'; }

      fetch(AB_CTX + '/AutoBidServlet', {
        method:  'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body:    'action=enable&itemId=' + AB_ITEM_ID + '&maxTarget=' + targetVal
      })
      .then(function(r) { return r.json(); })
      .then(function(data) {
        if (btn) { btn.disabled = false; btn.textContent = '\u2713 Activate Auto Bid'; }
        closeAutoBidModal();
        if (data.ok) {
          abActive    = true;
          abMaxTarget = targetVal;
          showAutoBidActive();
          showAutoBidToast('\u2705 Auto Bid activated! Target: ' +
            String.fromCharCode(8377) + Number(targetVal).toLocaleString('en-IN'), 'success');
        } else {
          showAutoBidToast('\u274c ' + (data.msg || 'Could not activate. Try again.'), 'error');
        }
      })
      .catch(function() {
        if (btn) { btn.disabled = false; btn.textContent = '\u2713 Activate Auto Bid'; }
        closeAutoBidModal();
        showAutoBidToast('\u274c Network error. Please try again.', 'error');
      });
    }

    // ── CANCEL Auto Bid ───────────────────────────────────────────────────────
    function cancelAutoBid() {
      fetch(AB_CTX + '/AutoBidServlet', {
        method:  'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body:    'action=cancel&itemId=' + AB_ITEM_ID
      })
      .then(function(r) { return r.json(); })
      .then(function(data) {
        showAutoBidInactive();
        showAutoBidToast('\uD83D\uDED1 Auto Bid disabled successfully.', 'info');
      })
      .catch(function() {
        showAutoBidToast('\u274c Network error. Please try again.', 'error');
      });
    }

    // ── 30-Second Countdown ───────────────────────────────────────────────────
    function startAutoBidCountdown(bidderName) {
      if (!abActive)                  return; // auto bid is off
      if (bidderName === AB_MY_USER)  return; // I placed the bid, skip
      if (abProcessing)               return; // already counting

      abProcessing   = true;
      abCountdownSec = 30;
      abLastBidder   = bidderName;

      var timerEl = document.getElementById('auto-bid-timer');
      var countEl = document.getElementById('ab-countdown');
      timerEl.classList.add('visible');
      countEl.textContent = abCountdownSec;

      abCountdownTimer = setInterval(function() {
        abCountdownSec--;
        countEl.textContent = abCountdownSec;
        if (abCountdownSec <= 0) {
          stopAutoBidCountdown();
          timerEl.classList.remove('visible');
          triggerAutoBid();
        }
      }, 1000);
    }

    function stopAutoBidCountdown() {
      clearInterval(abCountdownTimer);
      abCountdownTimer = null;
      abProcessing     = false;
      var timerEl = document.getElementById('auto-bid-timer');
      if (timerEl) timerEl.classList.remove('visible');
    }

    // ── TRIGGER Auto Bid on server ────────────────────────────────────────────
    function triggerAutoBid() {
      if (!abActive) return;

      showAutoBidToast('\uD83E\uDD16 Placing auto bid...', 'info');

      fetch(AB_CTX + '/AutoBidProcessorServlet', {
        method:  'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body:    'itemId=' + AB_ITEM_ID + '&lastBidder=' + encodeURIComponent(abLastBidder)
      })
      .then(function(r) { return r.json(); })
      .then(function(data) {
        abProcessing = false; // allow next countdown
        if (data.triggered) {
          showAutoBidToast(
            '\u2705 Auto bid placed by ' + data.bidder + ': ' +
            String.fromCharCode(8377) + Number(data.amount).toLocaleString('en-IN'),
            'success'
          );
        }
        
        // ALWAYS Re-check status from server.
        // Kyunki agar AutoBidProcessorServlet me bid price Target se zyada chali gayi, 
        // toh server use wahin deactivate kar dega aur 'triggered' false bhejega.
        setTimeout(function() {
          fetch(AB_CTX + '/AutoBidServlet?itemId=' + AB_ITEM_ID)
            .then(function(r) { return r.json(); })
            .then(function(st) {
              if (abActive && !st.active) {
                // Pehle active tha, ab server ne band kar diya
                showAutoBidInactive();
                showAutoBidToast(
                  '\u26A0\uFE0F Auto Bid stopped: Bid amount exceeded your target price!', 'error');
              } else if (st.active) {
                abMaxTarget = st.maxTarget;
              }
            }).catch(function() {});
        }, 1500); // Thoda ruk ke verify karo
      })
      .catch(function() {
        abProcessing = false;
        showAutoBidToast('\u274c Auto bid error. Please try again.', 'error');
      });
    }

    // ── Hook: called by bid-live.js when a new bid is detected ───────────────
    window.onNewBidDetected = function(bidderName, amount) {
      if (abActive && bidderName !== AB_MY_USER) {
        startAutoBidCountdown(bidderName);
      }
    };

    // ── Toast ─────────────────────────────────────────────────────────────────
    function showAutoBidToast(msg, type) {
      var toast = document.getElementById('auto-bid-toast');
      if (!toast) return;
      toast.textContent = msg;
      toast.className   = 'auto-bid-toast ' + (type || 'info') + ' show';
      clearTimeout(abToastTimer);
      abToastTimer = setTimeout(function() {
        toast.classList.remove('show');
      }, 4500);
    }

    // ── ESC closes modal ──────────────────────────────────────────────────────
    document.addEventListener('keydown', function(e) {
      if (e.key === 'Escape') closeAutoBidModal();
    });
  </script>

</body>
</html>