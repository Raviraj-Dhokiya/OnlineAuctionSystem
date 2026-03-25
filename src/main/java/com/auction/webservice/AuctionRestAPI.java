package com.auction.webservice;

import com.auction.dao.AuctionItemDAO;
import com.auction.dao.BidDAO;
import com.auction.dao.WinnerDAO;
import com.auction.model.*;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;

/**
 * ════════════════════════════════════════════════════════
 *  AuctionRestAPI.java — RESTful Web Service (Unit 7)
 * ════════════════════════════════════════════════════════
 *
 * REST API KYA HOTA HAI?
 *   - REST = App ka data dusre format (jaise JSON) mein baahar dena.
 *   - Isse aapki Auction app ka data mobile apps (Android/iOS) ya 
 *     kisi dusre server par directly padha/likha ja sakta hai bina HTML pages ke.
 *
 * ENDPOINTS (URL Raste):
 *   GET  /api/items           — Sab data dikhao
 *   GET  /api/items/{id}      — Ek specific item dikhao
 *   GET  /api/items/{id}/bids — Ek item ki bids dikhao
 *   POST /api/bids            — Nayi bid post karo
 *   GET  /api/winners         — Sab winners dikhao
 *
 * @Path  : URL set karne ke liye
 * @GET / @POST : Type of Request
 * @Produces(MediaType.APPLICATION_JSON) : Hamesha JSON text hi return karega
 * ════════════════════════════════════════════════════════
 */
@Path("/items")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuctionRestAPI {

    private final AuctionItemDAO itemDAO   = new AuctionItemDAO();
    private final BidDAO         bidDAO    = new BidDAO();
    private final WinnerDAO      winnerDAO = new WinnerDAO();

    // ── GET /api/items ───────────────────────────────────────────────────────

    @GET
    public Response getAllActiveItems() {
        List<AuctionItem> items = itemDAO.getActiveItems();
        return Response.ok(items).build();
    }

    // ── GET /api/items/{id} ──────────────────────────────────────────────────

    @GET
    @Path("/{id}")
    public Response getItem(@PathParam("id") int itemId) {
        AuctionItem item = itemDAO.findById(itemId);
        if (item == null)
            return Response.status(Response.Status.NOT_FOUND)
                           .entity("{\"error\":\"Item not found\"}").build();
        return Response.ok(item).build();
    }

    // ── GET /api/items/{id}/bids ─────────────────────────────────────────────

    @GET
    @Path("/{id}/bids")
    public Response getBids(@PathParam("id") int itemId) {
        List<Bid> bids = bidDAO.getBidsForItem(itemId);
        return Response.ok(bids).build();
    }

    // ── GET /api/items/search?q=guitar ──────────────────────────────────────

    @GET
    @Path("/search")
    public Response searchItems(@QueryParam("q") String keyword) {
        if (keyword == null || keyword.isEmpty())
            return Response.status(Response.Status.BAD_REQUEST)
                           .entity("{\"error\":\"Query required\"}").build();
        List<AuctionItem> items = itemDAO.searchItems(keyword);
        return Response.ok(items).build();
    }
}
