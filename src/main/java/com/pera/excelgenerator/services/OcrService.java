package com.pera.excelgenerator.services;

import lombok.RequiredArgsConstructor;
import models.ExcelViewModel;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.poi.util.StringUtil;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RequiredArgsConstructor
@Service
public class OcrService {

    private final ExcelService excelService;

    //gelen pdfi alır ve her bir sayfayı tarar geriye taranmış sayfaları liste halinde döner
    public List<BufferedImage> renderAllPagesAsImages(MultipartFile file) throws Exception {
        List<BufferedImage> images = new ArrayList<>();
        try (InputStream inputStream = file.getInputStream();
             PDDocument document = PDDocument.load(inputStream)) {
            PDFRenderer renderer = new PDFRenderer(document);
            int numPages = document.getNumberOfPages();
            for (int pageIndex = 0; pageIndex < numPages; pageIndex++) {
                BufferedImage image = renderer.renderImageWithDPI(pageIndex, 300);
                images.add(image);
            }
            BufferedImage image = renderer.renderImageWithDPI(0, 300);
            document.close();
            return images;
        }
    }

    public String extractTextWithOCR(BufferedImage image) throws TesseractException {
        ITesseract tesseract = new Tesseract();
        tesseract.setDatapath("C:\\Program Files\\Tesseract-OCR\\tessdata");
        tesseract.setLanguage("eng");
        return tesseract.doOCR(image);
    }


    public ExcelViewModel extractFieldsFromText(String text) {
        ExcelViewModel excelViewModel = new ExcelViewModel();

        Matcher gtip = Pattern.compile("CUSTOMS\\s*NOMENCLATURE\\s*(\\d{10})").matcher(text);
        if (gtip.find()) excelViewModel.setGtip(gtip.group(1));
        Matcher weight = Pattern.compile("TOTAL WEIGHT\\s*[:：]?\\s*([\\d.,]+)\\s*GR").matcher(text);
        if (weight.find()) {
            excelViewModel.setNetWeigth(weight.group(1));
            excelViewModel.setGrossWeigth(weight.group(1));
        }
        Matcher origin = Pattern.compile("Country of Origin\\s*[:：]?\\s*(\\w{2})").matcher(text);
        if (origin.find()) excelViewModel.setOrigin(origin.group(1));

        String[] lines = text.toUpperCase().split("\\r?\\n");
        String productCode = null;
        String productName = null;

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();

            if (line.contains("EXPEDITION NUMBER") || line.contains("ORDER")) {
                for (int j = i + 1; j <= i + 5 && j < lines.length; j++) {
                    String candidateLine = lines[j].trim();

                    Matcher m = Pattern.compile("^([A-Z0-9]{6,})\\s+([A-Z][A-Z\\s]{3,})").matcher(candidateLine);
                    if (m.find()) {
                        productCode = m.group(1).trim();

                        String rawName = m.group(2).trim();
                        productName = rawName.replaceAll("\\s+\\d+%.*", "").trim();

                        if (productName.split("\\s+").length < 2) {
                            productName = null;
                            productCode = null;
                        } else {
                            break;
                        }
                    }
                }
            }

            if (line.startsWith("PX") && i + 1 < lines.length) {
                Matcher m = Pattern.compile("^([A-Z0-9]{6,})\\s+([A-Z][A-Z\\s]{3,})").matcher(lines[i + 1].trim());
                if (m.find()) {
                    productCode = m.group(1).trim();
                    productName = m.group(2).trim();
                    break;
                }
            }

            if (productCode != null && productName != null) break;
        }

        if (productCode != null && productName != null) {
            excelViewModel.setProductCode(productCode);
            excelViewModel.setProductName(productName);
        }


        List<String> allPrices = new ArrayList<>();
        Matcher priceMatcher = Pattern.compile("\\b\\d{1,3}(?:\\.\\d{3})*,\\d{2}\\b").matcher(text);
        while (priceMatcher.find()) {
            allPrices.add(priceMatcher.group());
        }


        String ocrQuantity = null;
        Matcher qtyMatcher = Pattern.compile("(?:Qty|Quantity)\\s*[:：]?\\s*(\\d+)").matcher(text);
        if (qtyMatcher.find()) {
            ocrQuantity = qtyMatcher.group(1);
        } else {
            Matcher looseQtyMatcher = Pattern.compile("\\b(\\d+)\\s+[0-9]{1,3}(?:\\.\\d{3})*,[0-9]{2}").matcher(text);
            if (looseQtyMatcher.find()) {
                ocrQuantity = looseQtyMatcher.group(1);
            }
        }

        if (allPrices.size() >= 2) {
            try {
                double price1 = Double.parseDouble(allPrices.get(0).replace(".", "").replace(",", "."));
                double price2 = Double.parseDouble(allPrices.get(1).replace(".", "").replace(",", "."));

                String unitPriceStr;
                String amountStr;
                double unitPriceParsed;
                double amountParsed;

                if (price1 >= price2) {
                    amountStr = allPrices.get(0);
                    unitPriceStr = allPrices.get(1);
                    amountParsed = price1;
                    unitPriceParsed = price2;
                } else {
                    amountStr = allPrices.get(1);
                    unitPriceStr = allPrices.get(0);
                    amountParsed = price2;
                    unitPriceParsed = price1;
                }

                excelViewModel.setUnitPrice(unitPriceStr);
                excelViewModel.setTotalAmount(amountStr);

                int finalQty;

                if (Math.abs(unitPriceParsed - amountParsed) < 0.01) {
                    finalQty = 1;
                } else {
                    int calculatedQty = (int) Math.round(amountParsed / unitPriceParsed);
                    double recalculatedTotal = calculatedQty * unitPriceParsed;

                    if (Math.abs(recalculatedTotal - amountParsed) < 0.5 && calculatedQty > 0) {
                        finalQty = calculatedQty;
                    } else {
                        finalQty = 1; // Fallback
                    }
                }

                excelViewModel.setQuantity(String.valueOf(finalQty));

            } catch (NumberFormatException e) {
                e.printStackTrace(); // Hatalı format varsa
            }
        }

        return excelViewModel;
    }

    public String generateExcelFromPdf(MultipartFile file) throws Exception {

        List<ExcelViewModel> list = new ArrayList<>();
        List<BufferedImage> images = renderAllPagesAsImages(file);

        for (BufferedImage image : images) {
            String ocrText = extractTextWithOCR(image);
            ExcelViewModel excelViewModel = extractFieldsFromText(ocrText);
            if (StringUtil.isNotBlank(excelViewModel.getGtip()) && StringUtil.isNotBlank(excelViewModel.getOrigin())) {
                list.add(excelViewModel);
            }
        }
        return excelService.generateExcel(list);
    }

}
