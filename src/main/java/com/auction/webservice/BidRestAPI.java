package com.auction.webservice;

import com.auction.dao.BidDAO;
import com.auction.model.Bid;
import com.auction.model.User;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * BidRestAPI - Unit 7 (REST POST endpoint)
 */
@Path("/bids")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BidRestAPI {

    private final BidDAO bidDAO = new BidDAO();

    /**
     * POST /api/bids
     * Body: {"itemId":1, "bidAmount":500.00}
     */
    @POST
    public Response placeBid(BidRequest request, @Context HttpServletRequest httpReq) {
        HttpSession session = httpReq.getSession(false);
        if (session == null || session.getAttribute("loggedUser") == null) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        User user = (User) session.getAttribute("loggedUser");

        if (request == null || request.getItemId() <= 0 || request.getBidAmount() <= 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                           .entity("{\"error\":\"Invalid bid data\"}").build();
        }

        Bid bid = new Bid(request.getItemId(),
                          user.getUserId(),
                          request.getBidAmount());
        boolean success = bidDAO.placeBid(bid);

        if (success) {
            return Response.status(Response.Status.CREATED)
                           .entity("{\"status\":\"Bid placed successfully\"}").build();
        } else {
            return Response.status(Response.Status.CONFLICT)
                           .entity("{\"error\":\"Bid too low or auction closed\"}").build();
        }
    }

    // ── Inner DTO for bid request ────────────────────────────────────────────
    public static class BidRequest {
        private int    itemId;
        private int    bidderId;
        private double bidAmount;

        public int    getItemId()             { return itemId; }
        public void   setItemId(int i)        { this.itemId = i; }
        public int    getBidderId()           { return bidderId; }
        public void   setBidderId(int b)      { this.bidderId = b; }
        public double getBidAmount()          { return bidAmount; }
        public void   setBidAmount(double a)  { this.bidAmount = a; }
    }
}
