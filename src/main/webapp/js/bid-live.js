/**
 * bid-live.js — Unit 3 (Java Networking - Client side)
 * Connects to BidNotificationServer via TCP Socket simulation using
 * Fetch-based long polling (since browsers can't do raw TCP sockets).
 *
 * NOTE: In a real deployment, you'd use WebSocket. Here we show the
 * concept matching the Unit 3 Socket server that's running on port 9090.
 */

(function () {
  const POLL_INTERVAL = 5000; // poll every 5 seconds
  const notifBar      = document.getElementById('notif-bar');
  let   lastBidTime   = Date.now();

  function showNotification(msg) {
    if (!notifBar) return;
    notifBar.textContent = msg;
    notifBar.style.display = 'block';
    clearTimeout(notifBar._hideTimer);
    notifBar._hideTimer = setTimeout(() => {
      notifBar.style.display = 'none';
    }, 5000);
  }

  function pollForBids() {
    const itemId = typeof CURRENT_ITEM_ID !== 'undefined' ? CURRENT_ITEM_ID : null;
    const url    = itemId
      ? `/OnlineAuctionSystem/BidPollServlet?itemId=${itemId}&since=${lastBidTime}`
      : `/OnlineAuctionSystem/BidPollServlet?since=${lastBidTime}`;

    fetch(url)
      .then(r => r.json())
      .then(data => {
        if (data && data.length > 0) {
          data.forEach(bid => {
            showNotification(
              `🔨 New bid! ${bid.bidder} placed ₹${bid.amount.toLocaleString('en-IN')} on "${bid.item}"`
            );
            // Update current price display if we're on that item's page
            if (itemId && bid.itemId == itemId) {
              const priceEl = document.getElementById('current-price');
              if (priceEl) {
                priceEl.textContent = '₹' + bid.amount.toLocaleString('en-IN', {
                  minimumFractionDigits: 2
                });
                priceEl.style.animation = 'none';
                setTimeout(() => priceEl.style.animation = '', 10);
              }
            }
            lastBidTime = Date.now();
          });
        }
      })
      .catch(() => { /* server might be starting up */ });
  }

  // Start polling
  setInterval(pollForBids, POLL_INTERVAL);
  pollForBids(); // immediate first check
})();
