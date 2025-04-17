package com.pera.excelgenerator.services;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ExcelService {
    public String extractText(MultipartFile file) throws Exception {
        try (InputStream inputStream = file.getInputStream();
             PDDocument document = PDDocument.load(inputStream)) {

            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }

    public String extractGtipNumber(MultipartFile file) throws Exception {
        String text = extractText(file);
        Pattern gtipPattern = Pattern.compile("GTIP[:\\s]*([0-9]{10})");
        Matcher matcher = gtipPattern.matcher(text);

        if (matcher.find()) {
            return matcher.group(1);
        } else {
            return null;
        }
    }



    public BufferedImage renderFirstPageAsImage(MultipartFile file) throws Exception {
        try (InputStream inputStream = file.getInputStream();
             PDDocument document = PDDocument.load(inputStream)) {

            PDFRenderer renderer = new PDFRenderer(document);
            BufferedImage image = renderer.renderImageWithDPI(0, 300); // İlk sayfa, 300 DPI kalitede
            document.close();
            return image;
        }
    }

    public String extractTextWithOCR(BufferedImage image) throws TesseractException {
        ITesseract tesseract = new Tesseract();
        tesseract.setDatapath("C:\\Program Files\\Tesseract-OCR\\tessdata");
        tesseract.setLanguage("eng");

        return tesseract.doOCR(image);
    }

    public Map<String, String> extractInvoiceFieldsFromText(String text) {
        Map<String, String> result = new HashMap<>();

        // GTIP (10 haneli numara)
        Matcher gtip = Pattern.compile("CUSTOMS\\s*NOMENCLATURE\\s*(\\d{10})").matcher(text);
        if (gtip.find()) result.put("gtip", gtip.group(1));

        // Ağırlık
        Matcher weight = Pattern.compile("TOTAL WEIGHT\\s*[:：]?\\s*([\\d.,]+)\\s*GR").matcher(text);
        if (weight.find()) result.put("weight", weight.group(1));

        // Menşei
        Matcher origin = Pattern.compile("Country of Origin\\s*[:：]?\\s*(\\w{2})").matcher(text);
        if (origin.find()) result.put("origin", origin.group(1));

        // Ürün adı (EXPEDITION NUMBER ve PX25HDEAGYQ arasında)
        Matcher product = Pattern.compile("EXPEDITION NUMBER\\s+\\d+\\s+([A-Z\\s]+)\\s+(?=PX25HDEAGYQ)").matcher(text);
        if (product.find()) result.put("product", product.group(1).trim());

        // Quantity (1 sayısı var)
        Matcher qty = Pattern.compile("Quantity\\s*[:：]?\\s*(\\d+)").matcher(text);
        if (!qty.find()) qty = Pattern.compile("Qty\\s*[:：]?\\s*(\\d+)").matcher(text);
        if (qty.find()) result.put("qty", qty.group(1));

        // Unit Price ve Total Amount (COLOUR'dan sonra gelen fiyatları alıyoruz)
        Matcher priceMatcher = Pattern.compile("([0-9]+(?:\\.[0-9]{3})*(?:,[0-9]{2}))").matcher(text);
        List<String> prices = new ArrayList<>();
        while (priceMatcher.find()) {
            prices.add(priceMatcher.group(1));
        }

        // Unit Price (ilk fiyat)
        if (!prices.isEmpty()) result.put("unitPrice", prices.get(0));

        // Total Amount (ikinci fiyat)
        if (prices.size() > 1) result.put("totalAmount", prices.get(1));

        return result;
    }

}
