package com.ticketbus.ticketing.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.ticketbus.common.domain.Product;
import com.ticketbus.common.domain.Ticket;
import com.ticketbus.ticketing.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

@Slf4j
@Service
@RequiredArgsConstructor
public class TicketPrintService {

    private final ProductRepository productRepository;
    private final TicketingService ticketingService;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /**
     * Génère un PDF imprimable pour le ticket avec QR code
     */
    public byte[] generateTicketPdf(Long ticketId) throws IOException, WriterException {
        Ticket ticket = ticketingService.getTicket(ticketId);
        Product product = productRepository.findById(ticket.getProductId())
            .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        String productName = product.getName();
        String qrPayload = ticketingService.toDto(ticket, productName).getQrPayload();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document document = new Document(pdfDoc, PageSize.A6);

        try {
            // Fonts
            PdfFont headerFont = PdfFontFactory.createFont();
            PdfFont normalFont = PdfFontFactory.createFont();

            // Header
            document.add(new Paragraph("🎫 TICKET BUS")
                .setFont(headerFont)
                .setFontSize(20)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(10));

            document.add(new Paragraph("Ticket #" + ticket.getId())
                .setFont(normalFont)
                .setFontSize(14)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(15));

            // Ticket Information Table
            Table infoTable = new Table(2);
            infoTable.setWidth(UnitValue.createPercentValue(100));

            addInfoRow(infoTable, "Produit:", productName, normalFont);
            addInfoRow(infoTable, "Type:", product.getType().toString(), normalFont);
            addInfoRow(infoTable, "Prix:", product.getPrice() + " XAF", normalFont);
            addInfoRow(infoTable, "Usages:", ticket.getMaxUsage() >= 999 ? "Illimité" :
                String.valueOf(ticket.getMaxUsage()), normalFont);
            addInfoRow(infoTable, "Restants:", ticket.getMaxUsage() >= 999 ? "∞" :
                String.valueOf(ticket.getMaxUsage() - ticket.getUsageCount()), normalFont);
            addInfoRow(infoTable, "Valide du:", ticket.getValidFrom().format(FORMATTER), normalFont);
            addInfoRow(infoTable, "Expire le:", ticket.getValidUntil().format(FORMATTER), normalFont);
            addInfoRow(infoTable, "Statut:", ticket.getStatus().toString(), normalFont);
            addInfoRow(infoTable, "Zone:", ticket.getZone() != null ? ticket.getZone() : "—", normalFont);

            document.add(infoTable);

            // QR Code
            byte[] qrCodeBytes = generateQrCodeBytes(qrPayload, 200, 200);
            ImageData qrImageData = ImageDataFactory.create(qrCodeBytes);
            Image qrImage = new Image(qrImageData);
            qrImage.setTextAlignment(TextAlignment.CENTER);
            qrImage.setMarginTop(15);

            document.add(new Paragraph()
                .add(qrImage)
                .setTextAlignment(TextAlignment.CENTER));

            // Footer
            document.add(new Paragraph("Présentez ce QR code lors du contrôle")
                .setFont(normalFont)
                .setFontSize(10)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(15)
                .setItalic());

            document.add(new Paragraph("TicketBus © 2026")
                .setFont(normalFont)
                .setFontSize(8)
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(ColorConstants.GRAY)
                .setMarginTop(10));

        } finally {
            document.close();
        }

        return baos.toByteArray();
    }

    /**
     * Génère une image QR code en bytes
     */
    public byte[] generateQrCodeImage(String qrPayload, int width, int height) throws WriterException, IOException {
        return generateQrCodeBytes(qrPayload, width, height);
    }

    /**
     * Génère le QR code en base64 pour l'affichage web
     */
    public String generateQrCodeBase64(String qrPayload, int width, int height) throws WriterException, IOException {
        byte[] qrBytes = generateQrCodeBytes(qrPayload, width, height);
        return Base64.getEncoder().encodeToString(qrBytes);
    }

    private byte[] generateQrCodeBytes(String text, int width, int height) throws WriterException, IOException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
        return outputStream.toByteArray();
    }

    private void addInfoRow(Table table, String label, String value, PdfFont font) {
        table.addCell(new Cell()
            .add(new Paragraph(label)
                .setFont(font)
                .setFontSize(10)
                .setBold())
            .setBorder(null)
            .setPadding(3));

        table.addCell(new Cell()
            .add(new Paragraph(value)
                .setFont(font)
                .setFontSize(10))
            .setBorder(null)
            .setPadding(3));
    }
}
