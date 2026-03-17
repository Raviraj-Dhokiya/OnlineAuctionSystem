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
      background: rgba(15,23,42,0.6);
      border: 1px solid rgba(255,255,255,0.08);
      border-radius: 14px;
      overflow: hidden;
    }
    .chat-header {
      background: linear-gradient(135deg, #1e3a5f, #0f2d4a);
      padding: 0.9rem 1.2rem;
      display: flex;
      align-items: center;
      gap: 0.6rem;
      font-weight: 600;
      font-size: 0.95rem;
      color: #e2e8f0;
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
      background: rgba(0,0,0,0.2);
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
      background: linear-gradient(135deg, #3b82f6, #8b5cf6);
      display: flex; align-items: center; justify-content: center;
      font-size: 0.75rem; font-weight: 700;
      color: white; flex-shrink: 0;
    }
    .chat-msg .bubble {
      background: rgba(255,255,255,0.07);
      border-radius: 10px 10px 10px 2px;
      padding: 0.45rem 0.75rem;
      max-width: 85%;
    }
    .chat-msg.mine .bubble {
      background: rgba(59,130,246,0.25);
      border-radius: 10px 10px 2px 10px;
      margin-left: auto;
    }
    .chat-msg.mine { flex-direction: row-reverse; }
    .chat-msg .sender {
      font-size: 0.72rem;
      font-weight: 600;
      color: #60a5fa;
      margin-bottom: 2px;
    }
    .chat-msg.mine .sender { color: #a78bfa; text-align: right; }
    .chat-msg .text {
      font-size: 0.85rem;
      color: #cbd5e1;
      word-break: break-word;
    }
    .chat-msg .ts {
      font-size: 0.68rem;
      color: #475569;
      margin-top: 2px;
      text-align: right;
    }
    .chat-msg.system-msg {
      justify-content: center;
    }
    .chat-msg.system-msg .bubble {
      background: transparent;
      font-size: 0.75rem;
      color: #64748b;
      font-style: italic;
      text-align: center;
      padding: 0.2rem 0.5rem;
    }
    .chat-input-row {
      display: flex;
      gap: 0.5rem;
      padding: 0.75rem 1rem;
      background: rgba(0,0,0,0.15);
      border-top: 1px solid rgba(255,255,255,0.06);
    }
    .chat-input-row input {
      flex: 1;
      background: rgba(255,255,255,0.06);
      border: 1px solid rgba(255,255,255,0.12);
      border-radius: 8px;
      padding: 0.5rem 0.8rem;
      color: #e2e8f0;
      font-size: 0.88rem;
      outline: none;
      transition: border-color 0.2s;
    }
    .chat-input-row input:focus {
      border-color: #3b82f6;
      background: rgba(59,130,246,0.08);
    }
    .chat-input-row button {
      padding: 0.5rem 1rem;
      background: #3b82f6;
      border: none;
      border-radius: 8px;
      color: white;
      font-size: 0.88rem;
      cursor: pointer;
      transition: background 0.2s;
    }
    .chat-input-row button:hover { background: #2563eb; }
    .chat-reconnect-bar {
      background: #7c2d12;
      color: #fca5a5;
      font-size: 0.78rem;
      text-align: center;
      padding: 0.3rem;
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
      cursor: pointer;
      text-decoration: none;
      transition: all 0.2s;
      border: 1px solid rgba(255,255,255,0.15);
      margin-top: 0.8rem;
    }
    .watchlist-btn.watching {
      background: rgba(251,191,36,0.15);
      color: #fbbf24;
      border-color: #fbbf24;
    }
    .watchlist-btn.not-watching {
      background: rgba(255,255,255,0.06);
      color: #94a3b8;
    }
    .watchlist-btn:hover { opacity: 0.85; transform: translateY(-1px); }

    /* ── Min Bid Suggestion ─────────────────────────────────────────────── */
    .min-bid-hint {
      background: linear-gradient(135deg, rgba(59,130,246,0.12), rgba(139,92,246,0.12));
      border: 1px solid rgba(59,130,246,0.3);
      border-radius: 8px;
      padding: 0.6rem 1rem;
      margin-bottom: 0.8rem;
      font-size: 0.85rem;
      color: #93c5fd;
      display: flex;
      align-items: center;
      gap: 0.5rem;
    }
    .min-bid-hint strong { color: #60a5fa; font-size: 1rem; }

    /* ── Time Extension Notice ──────────────────────────────────────────── */
    .extension-notice {
      background: rgba(234,179,8,0.12);
      border: 1px solid rgba(234,179,8,0.35);
      border-radius: 8px;
      padding: 0.55rem 0.9rem;
      font-size: 0.82rem;
      color: #fde047;
      margin-top: 0.5rem;
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
    // ─── Item & User Context ───────────────────────────────────────────────────
    const CURRENT_ITEM_ID = ${ item.itemId };
    const MY_USERNAME     = "${sessionScope.username}";
    const CHAT_PORT       = 9092;
    const CHAT_HOST       = location.hostname; // same server

    // ─── Chat via WebSocket bridge (HTTP polling fallback) ─────────────────────
    // AuctionChatServer is a raw TCP socket server on port 9092.
    // Since browsers cannot use raw TCP sockets, we poll a /ChatPollServlet
    // for messages and post via /ChatSendServlet.
    // This gives a smooth chat experience without requiring WebSocket upgrade
    // of the existing AuctionChatServer.

    const CTX = "${pageContext.request.contextPath}";
    let lastMsgId  = 0;
    let pollTimer  = null;
    let chatReady  = false;
    const chatDot  = document.getElementById('chat-dot');
    const chatLabel = document.getElementById('chat-status-label');
    const chatInput = document.getElementById('chat-input');
    const chatSend  = document.getElementById('chat-send-btn');
    const chatBox   = document.getElementById('chat-messages');
    const reconnBar = document.getElementById('chat-reconnect-bar');

    function setChatOnline() {
      chatDot.style.background   = '#22c55e';
      chatDot.style.boxShadow    = '0 0 6px #22c55e';
      chatLabel.textContent      = 'Connected';
      chatLabel.style.color      = '#22c55e';
      chatInput.disabled         = false;
      chatSend.disabled          = false;
      reconnBar.style.display    = 'none';
      chatReady = true;
    }

    function setChatOffline() {
      chatDot.style.background   = '#ef4444';
      chatDot.style.boxShadow    = '0 0 6px #ef4444';
      chatLabel.textContent      = 'Offline';
      chatLabel.style.color      = '#ef4444';
      chatInput.disabled         = true;
      chatSend.disabled          = true;
      reconnBar.style.display    = 'block';
      chatReady = false;
    }

    // Render a single message into the chat box
    function appendMsg(sender, text, timestamp, isSystem) {
      const div = document.createElement('div');
      if (isSystem) {
        div.className = 'chat-msg system-msg';
        div.innerHTML = '<div class="bubble">' + escHtml(text) + '</div>';
      } else {
        const isMine = (sender === MY_USERNAME);
        div.className = 'chat-msg' + (isMine ? ' mine' : '');
        const initial = sender ? sender.charAt(0).toUpperCase() : '?';
        const ts = timestamp ? new Date(timestamp).toLocaleTimeString([], {hour:'2-digit',minute:'2-digit'}) : '';
        div.innerHTML =
          '<div class="avatar">' + escHtml(initial) + '</div>' +
          '<div class="bubble">' +
            '<div class="sender">' + escHtml(sender) + '</div>' +
            '<div class="text">'   + escHtml(text)   + '</div>' +
            '<div class="ts">'     + escHtml(ts)     + '</div>' +
          '</div>';
      }
      // Remove "Loading..." placeholder if it exists
      const loading = chatBox.querySelector('.system-msg');
      if (loading && loading.querySelector('.bubble') &&
          loading.querySelector('.bubble').textContent === 'Loading chat history...') {
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

    // ── Poll for new messages ──────────────────────────────────────────────────
    function pollMessages() {
      fetch(CTX + '/ChatPollServlet?itemId=' + CURRENT_ITEM_ID + '&since=' + lastMsgId)
        .then(r => r.json())
        .then(data => {
          if (!chatReady) setChatOnline();
          if (data && data.messages) {
            data.messages.forEach(m => {
              appendMsg(m.sender, m.content, m.sentAt, false);
              if (m.msgId > lastMsgId) lastMsgId = m.msgId;
            });
          }
        })
        .catch(() => {
          setChatOffline();
        });
    }

    // ── Send a message ─────────────────────────────────────────────────────────
    function sendChat() {
      const msg = chatInput.value.trim();
      if (!msg || !chatReady) return;
      chatInput.value = '';

      fetch(CTX + '/ChatSendServlet', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: 'itemId=' + CURRENT_ITEM_ID + '&content=' + encodeURIComponent(msg)
      })
      .then(r => r.json())
      .then(d => {
        if (d.ok) {
          // Message sent — next poll will show it (or show immediately)
          appendMsg(MY_USERNAME, msg, new Date().toISOString(), false);
        }
      })
      .catch(() => setChatOffline());
    }

    // Enter key sends message
    chatInput.addEventListener('keydown', e => {
      if (e.key === 'Enter') sendChat();
    });

    // Start polling every 2 seconds
    pollMessages(); // immediate first fetch
    pollTimer = setInterval(pollMessages, 2000);

    // Cleanup on page unload
    window.addEventListener('beforeunload', () => clearInterval(pollTimer));
  </script>

  <script src="${pageContext.request.contextPath}/js/bid-live.js"></script>


</body>
</html>