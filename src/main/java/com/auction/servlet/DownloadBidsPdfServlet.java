package com.auction.servlet;

import com.auction.dao.AuctionItemDAO;
import com.auction.dao.BidDAO;
import com.auction.model.AuctionItem;
import com.auction.model.Bid;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * ════════════════════════════════════════════════════════
 * DownloadBidsPdfServlet.java — Exports Bid History as PDF
 * ════════════════════════════════════════════════════════
 */
@WebServlet("/DownloadBidsPdfServlet")
public class DownloadBidsPdfServlet extends HttpServlet {

    private final AuctionItemDAO itemDAO = new AuctionItemDAO();
    private final BidDAO bidDAO = new BidDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        String itemIdStr = req.getParameter("itemId");
        if (itemIdStr == null || itemIdStr.isEmpty()) {
            res.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing itemId");
            return;
        }

        try {
            int itemId = Integer.parseInt(itemIdStr);
            AuctionItem item = itemDAO.findById(itemId);
            List<Bid> bids = bidDAO.getBidsForItem(itemId);

            if (item == null) {
                res.sendError(HttpServletResponse.SC_NOT_FOUND, "Item not found");
                return;
            }

            // Set browser response type to PDF file download
            res.setContentType("application/pdf");
            res.setHeader("Content-Disposition", "attachment; filename=\"Auction_" + itemId + "_Bids.pdf\"");

            Document document = new Document();
            PdfWriter.getInstance(document, res.getOutputStream());
            document.open();

            // ── Styling Elements ──
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, BaseColor.DARK_GRAY);
            Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA, 12, BaseColor.GRAY);
            Font tableHeaderFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.WHITE);

            // ── Document Header ──
            Paragraph title = new Paragraph("🏆 AuctionHub Official Report", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(10);
            document.add(title);

            Paragraph subtitle = new Paragraph("Bid History for: " + item.getTitle(), subtitleFont);
            subtitle.setAlignment(Element.ALIGN_CENTER);
            subtitle.setSpacingAfter(30);
            document.add(subtitle);

            // ── Item Details ──
            document.add(new Paragraph("Category: " + item.getCategory()));
            document.add(new Paragraph("Starting Price: Rs. " + item.getStartingPrice()));
            document.add(new Paragraph("Current Highest Bid: Rs. " + item.getCurrentPrice()));
            document.add(new Paragraph("Total Bids: " + bids.size()));
            document.add(new Paragraph(" ")); // Blank space

            // ── Bids Table ──
            if (bids.isEmpty()) {
                document.add(new Paragraph("No bids have been placed on this item yet."));
            } else {
                PdfPTable table = new PdfPTable(3);
                table.setWidthPercentage(100);
                table.setSpacingBefore(10f);
                table.setSpacingAfter(10f);
                table.setWidths(new float[]{3f, 4f, 4f});

                // Table Header
                PdfPCell cell1 = new PdfPCell(new Phrase("Bidder Name", tableHeaderFont));
                cell1.setBackgroundColor(new BaseColor(108, 99, 255)); // Primary color
                cell1.setPadding(8);
                
                PdfPCell cell2 = new PdfPCell(new Phrase("Bid Amount (Rs.)", tableHeaderFont));
                cell2.setBackgroundColor(new BaseColor(108, 99, 255));
                cell2.setPadding(8);
                
                PdfPCell cell3 = new PdfPCell(new Phrase("Time of Bid", tableHeaderFont));
                cell3.setBackgroundColor(new BaseColor(108, 99, 255));
                cell3.setPadding(8);

                table.addCell(cell1);
                table.addCell(cell2);
                table.addCell(cell3);

                SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, hh:mm a");

                // Table Rows
                for (Bid bid : bids) {
                    PdfPCell c1 = new PdfPCell(new Phrase(bid.getBidderName()));
                    c1.setPadding(6);
                    PdfPCell c2 = new PdfPCell(new Phrase(String.valueOf(bid.getBidAmount())));
                    c2.setPadding(6);
                    PdfPCell c3 = new PdfPCell(new Phrase(sdf.format(bid.getBidTime())));
                    c3.setPadding(6);

                    table.addCell(c1);
                    table.addCell(c2);
                    table.addCell(c3);
                }

                document.add(table);
            }

            // ── Footer ──
            document.add(new Paragraph(" "));
            Paragraph footer = new Paragraph("Generated by AuctionHub System", FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 10, BaseColor.GRAY));
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);

            document.close();

        } catch (Exception e) {
            e.printStackTrace();
            res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error generating PDF");
        }
    }
}
