package com.auction.webservice;

import com.auction.dao.WinnerDAO;
import com.auction.model.Winner;

import javax.ws.rs.*;
import javax.ws.rs.core.*;

/**
 * WinnerRestAPI - Unit 7 (REST)
 */
@Path("/winners")
@Produces(MediaType.APPLICATION_JSON)
public class WinnerRestAPI {

    private final WinnerDAO winnerDAO = new WinnerDAO();

    @GET
    public Response getAllWinners() {
        return Response.ok(winnerDAO.getAllWinners()).build();
    }

    @GET
    @Path("/item/{id}")
    public Response getWinnerForItem(@PathParam("id") int itemId) {
        Winner w = winnerDAO.getWinnerByItem(itemId);
        if (w == null)
            return Response.status(Response.Status.NOT_FOUND)
                           .entity("{\"error\":\"No winner yet\"}").build();
        return Response.ok(w).build();
    }
}
