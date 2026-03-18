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
  let   lastBidId     = 0;
  let   isFirstPoll   = true;

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
    const ctx = typeof CTX !== 'undefined' ? CTX : '/OnlineAuctionSystem';
    const url = itemId
      ? `${ctx}/BidPollServlet?itemId=${itemId}&lastBidId=${lastBidId}`
      : `${ctx}/BidPollServlet?lastBidId=${lastBidId}`;

    fetch(url)
      .then(r => r.json())
      .then(data => {
        if (data && data.length > 0) {
          data.forEach(bid => {
            if (bid.bidId > lastBidId) {
                lastBidId = bid.bidId;
            }
            
            if (!isFirstPoll) {
                showNotification(
                  `🔨 New bid! ${bid.bidder} placed ₹${bid.amount.toLocaleString('en-IN')} on "${bid.item}"`
                );
                // Update current price display AND table if we're on that item's page
                if (itemId && bid.itemId == itemId) {
                  const priceEl = document.getElementById('current-price');
                  if (priceEl) {
                    priceEl.textContent = '₹' + bid.amount.toLocaleString('en-IN', {
                      minimumFractionDigits: 2
                    });
                    priceEl.style.animation = 'none';
                    setTimeout(() => priceEl.style.animation = '', 10);
                  }
                  
                  const bidInput = document.getElementById('bidAmountInput');
                  if (bidInput) {
                    const minBidAmt = Math.ceil(bid.amount + Math.max(bid.amount * 0.05, 1));
                    bidInput.min = minBidAmt;
                    bidInput.placeholder = 'Min: ₹' + minBidAmt.toFixed(2);
                    
                    // Optional: Update min bid suggestion text
                    const hintStrong = document.querySelector('.min-bid-hint strong');
                    if (hintStrong) {
                      hintStrong.textContent = '₹' + minBidAmt.toLocaleString('en-IN', {minimumFractionDigits: 2});
                    }
                  }

                  const totalBidsEl = document.getElementById('total-bids');
                  if (totalBidsEl) {
                    totalBidsEl.textContent = parseInt(totalBidsEl.textContent || '0') + 1;
                  }

                  const tbody = document.getElementById('bid-history-body');
                  const emptyMsg = document.getElementById('empty-bids-msg');
                  const bidTable = document.getElementById('bid-table');

                  if (tbody) {
                    if (emptyMsg) emptyMsg.style.display = 'none';
                    if (bidTable) bidTable.style.display = 'table';

                    // Clean old crown
                    const prevRows = tbody.querySelectorAll('tr');
                    if (prevRows.length > 0) {
                      prevRows[0].className = '';
                      // remove crown from text
                      const tdToClean = prevRows[0].querySelector('td:first-child');
                      if (tdToClean) {
                        tdToClean.innerHTML = tdToClean.innerHTML.replace('👑', '');
                      }
                    }

                    // Append new row
                    const tr = document.createElement('tr');
                    tr.className = 'row-highlight';
                    
                    const tdBidder = document.createElement('td');
                    tdBidder.innerHTML = bid.bidder + ' 👑';

                    const tdAmount = document.createElement('td');
                    tdAmount.textContent = bid.amount.toLocaleString('en-IN', {
                      minimumFractionDigits: 2
                    });

                    const tdTime = document.createElement('td');
                    tdTime.textContent = bid.bidTimeStr || '';

                    tr.appendChild(tdBidder);
                    tr.appendChild(tdAmount);
                    tr.appendChild(tdTime);

                    tbody.insertBefore(tr, tbody.firstChild);
                  }
                  // ── AUTO BID HOOK ─────────────────────────────────────────────
                  // Agar auto bid active hai (item-detail.jsp script mein),
                  // naya bid detect karke 30-second countdown start karo
                  if (typeof window.onNewBidDetected === 'function') {
                    window.onNewBidDetected(bid.bidder, bid.amount);
                  }
                }
            } // end isFirstPoll
          });
        }
        isFirstPoll = false;
      })
      .catch(() => { /* server might be starting up */ });
  }

  // Start polling
  setInterval(pollForBids, POLL_INTERVAL);
  pollForBids(); // immediate first check
})();
