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
  </style>
</head>

<body>

  <nav class="navbar">
    <div class="nav-brand">🏆 AuctionHub</div>
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
    <c:if test="${param.watchAdded == '1'}">
      <div class="alert alert-success">⭐ Item added to your watchlist!</div>
    </c:if>
    <c:if test="${param.watchRemoved == '1'}">
      <div class="alert alert-success">Removed from watchlist.</div>
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
              <span class="value">${bidCount}</span>
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
                    <td>${bid.bidderName} <c:if test="${bid.winning}">👑</c:if></td>
                    <td><fmt:formatNumber value="${bid.bidAmount}" pattern="#,##0.00"/></td>
                    <td><fmt:formatDate value="${bid.bidTime}" pattern="dd MMM HH:mm:ss"/></td>
                  </tr>
                </c:forEach>
              </tbody>
            </table>
          </c:otherwise>
        </c:choose>

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

  <script src="${pageContext.request.contextPath}/js/bid-live.js"></script>


</body>
</html>