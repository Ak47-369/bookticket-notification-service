package com.bookticket.notification_service.service;

import com.bookticket.notification_service.dto.SeatDetails;
import com.bookticket.notification_service.dto.TicketDetails;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.lowagie.text.pdf.draw.LineSeparator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

@Service
@Slf4j
public class PdfService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("EEE, MMM dd, yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("hh:mm a");

    // Color scheme
    private static final Color PRIMARY_COLOR = new Color(102, 126, 234); // #667eea
    private static final Color SECONDARY_COLOR = new Color(118, 75, 162); // #764ba2
    private static final Color DARK_COLOR = new Color(33, 37, 41); // #212529
    private static final Color LIGHT_GRAY = new Color(248, 249, 250); // #f8f9fa
    private static final Color TEXT_GRAY = new Color(108, 117, 125); // #6c757d

    private final QRCodeService qrCodeService;

    public PdfService(QRCodeService qrCodeService) {
        this.qrCodeService = qrCodeService;
    }

    public byte[] generateTicketPdf(TicketDetails ticketDetails) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4);
            PdfWriter writer = PdfWriter.getInstance(document, baos);
            document.open();

            // Add header with gradient effect (simulated with colored rectangle)
            addHeader(document, ticketDetails);

            // Add booking ID section
            addBookingIdSection(document, ticketDetails);

            // Add movie details
            addMovieDetails(document, ticketDetails);

            // Add theater details
            addTheaterDetails(document, ticketDetails);

            // Add seat details table
            addSeatDetailsTable(document, ticketDetails);

            // Add QR code
            addQRCode(document, ticketDetails);

            // Add footer
            addFooter(document);

            document.close();
            log.info("Beautiful PDF generated successfully for booking: {}", ticketDetails.bookingId());
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Failed to generate PDF: {}", e.getMessage());
            throw new RuntimeException("Failed to generate PDF", e);
        }
    }

    private void addHeader(Document document, TicketDetails ticketDetails) throws DocumentException {
        // Create a colored rectangle for header background
        PdfPTable headerTable = new PdfPTable(1);
        headerTable.setWidthPercentage(100);
        headerTable.setSpacingAfter(20f);

        PdfPCell headerCell = new PdfPCell();
        headerCell.setBackgroundColor(PRIMARY_COLOR);
        headerCell.setPadding(20f);
        headerCell.setBorder(Rectangle.NO_BORDER);

        // Title
        Font titleFont = new Font(Font.HELVETICA, 28, Font.BOLD, Color.WHITE);
        Paragraph title = new Paragraph("🎬 MOVIE TICKET", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);

        Font subtitleFont = new Font(Font.HELVETICA, 12, Font.NORMAL, Color.WHITE);
        Paragraph subtitle = new Paragraph("BookTicket - Your Entertainment Partner", subtitleFont);
        subtitle.setAlignment(Element.ALIGN_CENTER);
        subtitle.setSpacingBefore(5f);

        headerCell.addElement(title);
        headerCell.addElement(subtitle);
        headerTable.addCell(headerCell);

        document.add(headerTable);
    }

    private void addBookingIdSection(Document document, TicketDetails ticketDetails) throws DocumentException {
        PdfPTable bookingTable = new PdfPTable(1);
        bookingTable.setWidthPercentage(100);
        bookingTable.setSpacingAfter(15f);

        PdfPCell bookingCell = new PdfPCell();
        bookingCell.setBackgroundColor(LIGHT_GRAY);
        bookingCell.setPadding(15f);
        bookingCell.setBorder(Rectangle.NO_BORDER);
        bookingCell.setHorizontalAlignment(Element.ALIGN_CENTER);

        Font labelFont = new Font(Font.HELVETICA, 10, Font.NORMAL, TEXT_GRAY);
        Paragraph label = new Paragraph("BOOKING ID", labelFont);
        label.setAlignment(Element.ALIGN_CENTER);

        Font idFont = new Font(Font.COURIER, 20, Font.BOLD, DARK_COLOR);
        Paragraph bookingId = new Paragraph("#" + ticketDetails.bookingId(), idFont);
        bookingId.setAlignment(Element.ALIGN_CENTER);
        bookingId.setSpacingBefore(5f);

        bookingCell.addElement(label);
        bookingCell.addElement(bookingId);
        bookingTable.addCell(bookingCell);

        document.add(bookingTable);
    }

    private void addMovieDetails(Document document, TicketDetails ticketDetails) throws DocumentException {
        PdfPTable movieTable = new PdfPTable(1);
        movieTable.setWidthPercentage(100);
        movieTable.setSpacingAfter(15f);

        PdfPCell movieCell = new PdfPCell();
        movieCell.setBackgroundColor(PRIMARY_COLOR);
        movieCell.setPadding(15f);
        movieCell.setBorder(Rectangle.NO_BORDER);

        Font movieFont = new Font(Font.HELVETICA, 22, Font.BOLD, Color.WHITE);
        Paragraph movieTitle = new Paragraph(ticketDetails.showDetails().movieTitle(), movieFont);
        movieTitle.setAlignment(Element.ALIGN_CENTER);

        String showDate = ticketDetails.showDetails().startTime().format(DATE_FORMATTER);
        String showTime = ticketDetails.showDetails().startTime().format(TIME_FORMATTER);

        Font dateTimeFont = new Font(Font.HELVETICA, 12, Font.NORMAL, Color.WHITE);
        Paragraph dateTime = new Paragraph("📅 " + showDate + "  |  🕐 " + showTime, dateTimeFont);
        dateTime.setAlignment(Element.ALIGN_CENTER);
        dateTime.setSpacingBefore(8f);

        movieCell.addElement(movieTitle);
        movieCell.addElement(dateTime);
        movieTable.addCell(movieCell);

        document.add(movieTable);
    }


    private void addTheaterDetails(Document document, TicketDetails ticketDetails) throws DocumentException {
        PdfPTable theaterTable = new PdfPTable(1);
        theaterTable.setWidthPercentage(100);
        theaterTable.setSpacingAfter(15f);

        PdfPCell theaterCell = new PdfPCell();
        theaterCell.setBackgroundColor(LIGHT_GRAY);
        theaterCell.setPadding(15f);
        theaterCell.setBorder(Rectangle.NO_BORDER);

        Font headerFont = new Font(Font.HELVETICA, 14, Font.BOLD, DARK_COLOR);
        Paragraph header = new Paragraph("🎭 THEATER DETAILS", headerFont);
        header.setSpacingAfter(10f);

        Font detailFont = new Font(Font.HELVETICA, 11, Font.NORMAL, DARK_COLOR);
        Font labelFont = new Font(Font.HELVETICA, 11, Font.BOLD, TEXT_GRAY);

        Paragraph theaterName = new Paragraph();
        theaterName.add(new Chunk("Theater: ", labelFont));
        theaterName.add(new Chunk(ticketDetails.showDetails().theaterName(), detailFont));
        theaterName.setSpacingAfter(5f);

        Paragraph address = new Paragraph();
        address.add(new Chunk("Address: ", labelFont));
        address.add(new Chunk(ticketDetails.showDetails().theaterAddress(), detailFont));
        address.setSpacingAfter(5f);

        Paragraph screen = new Paragraph();
        screen.add(new Chunk("Screen: ", labelFont));
        screen.add(new Chunk(ticketDetails.showDetails().screenName(), detailFont));

        theaterCell.addElement(header);
        theaterCell.addElement(theaterName);
        theaterCell.addElement(address);
        theaterCell.addElement(screen);
        theaterTable.addCell(theaterCell);

        document.add(theaterTable);
    }

    private void addSeatDetailsTable(Document document, TicketDetails ticketDetails) throws DocumentException {
        // Section header
        Font headerFont = new Font(Font.HELVETICA, 14, Font.BOLD, DARK_COLOR);
        Paragraph seatHeader = new Paragraph("🎫 YOUR SEATS", headerFont);
        seatHeader.setSpacingBefore(5f);
        seatHeader.setSpacingAfter(10f);
        document.add(seatHeader);

        // Create table
        PdfPTable table = new PdfPTable(3);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{2f, 2f, 1.5f});
        table.setSpacingAfter(15f);

        // Header row
        Font tableHeaderFont = new Font(Font.HELVETICA, 11, Font.BOLD, Color.WHITE);

        PdfPCell seatHeader1 = createTableHeaderCell("Seat Number", tableHeaderFont);
        PdfPCell seatHeader2 = createTableHeaderCell("Type", tableHeaderFont);
        PdfPCell seatHeader3 = createTableHeaderCell("Price", tableHeaderFont);

        table.addCell(seatHeader1);
        table.addCell(seatHeader2);
        table.addCell(seatHeader3);

        // Data rows
        Font dataFont = new Font(Font.HELVETICA, 10, Font.NORMAL, DARK_COLOR);
        double totalAmount = 0.0;

        for (SeatDetails seat : ticketDetails.seats()) {
            PdfPCell cell1 = createTableDataCell(seat.seatNumber(), dataFont);
            PdfPCell cell2 = createTableDataCell(seat.seatType(), dataFont);
            PdfPCell cell3 = createTableDataCell(String.format("₹%.2f", seat.price()), dataFont);

            table.addCell(cell1);
            table.addCell(cell2);
            table.addCell(cell3);

            totalAmount += seat.price();
        }

        // Total row
        Font totalFont = new Font(Font.HELVETICA, 12, Font.BOLD, DARK_COLOR);
        PdfPCell totalLabelCell = new PdfPCell(new Phrase("TOTAL AMOUNT", totalFont));
        totalLabelCell.setColspan(2);
        totalLabelCell.setBackgroundColor(LIGHT_GRAY);
        totalLabelCell.setPadding(10f);
        totalLabelCell.setBorder(Rectangle.NO_BORDER);
        totalLabelCell.setHorizontalAlignment(Element.ALIGN_RIGHT);

        Font totalAmountFont = new Font(Font.HELVETICA, 14, Font.BOLD, PRIMARY_COLOR);
        PdfPCell totalAmountCell = new PdfPCell(new Phrase(String.format("₹%.2f", totalAmount), totalAmountFont));
        totalAmountCell.setBackgroundColor(LIGHT_GRAY);
        totalAmountCell.setPadding(10f);
        totalAmountCell.setBorder(Rectangle.NO_BORDER);
        totalAmountCell.setHorizontalAlignment(Element.ALIGN_CENTER);

        table.addCell(totalLabelCell);
        table.addCell(totalAmountCell);

        document.add(table);
    }

    private PdfPCell createTableHeaderCell(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(PRIMARY_COLOR);
        cell.setPadding(10f);
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        return cell;
    }

    private PdfPCell createTableDataCell(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(8f);
        cell.setBorder(Rectangle.BOTTOM);
        cell.setBorderColor(LIGHT_GRAY);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        return cell;
    }

    private void addQRCode(Document document, TicketDetails ticketDetails) throws DocumentException {
        try {
            // Generate QR code data
            String qrData = qrCodeService.generateTicketData(
                    ticketDetails.bookingId(),
                    ticketDetails.userId(),
                    ticketDetails.showDetails().movieTitle()
            );

            // Generate QR code image
            byte[] qrCodeBytes = qrCodeService.generateQRCode(qrData, 200, 200);
            Image qrImage = Image.getInstance(qrCodeBytes);
            qrImage.scaleToFit(150, 150);
            qrImage.setAlignment(Element.ALIGN_CENTER);

            // QR code section
            Font qrHeaderFont = new Font(Font.HELVETICA, 12, Font.BOLD, DARK_COLOR);
            Paragraph qrHeader = new Paragraph("Scan for Verification", qrHeaderFont);
            qrHeader.setAlignment(Element.ALIGN_CENTER);
            qrHeader.setSpacingBefore(10f);
            qrHeader.setSpacingAfter(10f);

            document.add(qrHeader);
            document.add(qrImage);

        } catch (Exception e) {
            log.error("Failed to add QR code to PDF: {}", e.getMessage());
            // Continue without QR code if it fails
        }
    }

    private void addFooter(Document document) throws DocumentException {
        Font footerFont = new Font(Font.HELVETICA, 9, Font.NORMAL, TEXT_GRAY);

        Paragraph footer1 = new Paragraph("⚠️ Please arrive 15 minutes before showtime", footerFont);
        footer1.setAlignment(Element.ALIGN_CENTER);
        footer1.setSpacingBefore(20f);
        footer1.setSpacingAfter(5f);

        Paragraph footer2 = new Paragraph("Carry a valid ID proof for verification", footerFont);
        footer2.setAlignment(Element.ALIGN_CENTER);
        footer2.setSpacingAfter(5f);

        Paragraph footer3 = new Paragraph("For support: support@bookticket.com | +91 123 456 7890", footerFont);
        footer3.setAlignment(Element.ALIGN_CENTER);
        footer3.setSpacingAfter(10f);

        // Divider line
        LineSeparator line = new LineSeparator();
        line.setLineColor(LIGHT_GRAY);
        document.add(new Chunk(line));

        document.add(footer1);
        document.add(footer2);
        document.add(footer3);

        Font thankYouFont = new Font(Font.HELVETICA, 10, Font.BOLD, PRIMARY_COLOR);
        Paragraph thankYou = new Paragraph("Thank you for choosing BookTicket! Enjoy your movie! 🍿", thankYouFont);
        thankYou.setAlignment(Element.ALIGN_CENTER);
        thankYou.setSpacingBefore(5f);

        document.add(thankYou);
    }
}
