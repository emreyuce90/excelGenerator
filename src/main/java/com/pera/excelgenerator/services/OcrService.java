package com.pera.excelgenerator.services;

import lombok.RequiredArgsConstructor;
import models.ExcelViewModel;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
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


    public ExcelViewModel extractFieldsFromText(String text){
        ExcelViewModel excelViewModel = new ExcelViewModel();

        Matcher gtip = Pattern.compile("CUSTOMS\\s*NOMENCLATURE\\s*(\\d{10})").matcher(text);
        if (gtip.find()) excelViewModel.setGtip(gtip.group(1));
        Matcher weight = Pattern.compile("TOTAL WEIGHT\\s*[:：]?\\s*([\\d.,]+)\\s*GR").matcher(text);
        if (weight.find()){
            excelViewModel.setNetWeigth(weight.group(1));
            excelViewModel.setGrossWeigth(weight.group(1));
        }
        Matcher origin = Pattern.compile("Country of Origin\\s*[:：]?\\s*(\\w{2})").matcher(text);
        if (origin.find()) excelViewModel.setOrigin(origin.group(1));

        Matcher product = Pattern.compile("EXPEDITION NUMBER\\s+\\d+\\s+([A-Z\\s]+)\\s+(?=PX25HDEAGYQ)").matcher(text);
        if (product.find()) excelViewModel.setProductName(product.group(1).trim());

        Matcher qty = Pattern.compile("Quantity\\s*[:：]?\\s*(\\d+)").matcher(text);
        if (!qty.find()) qty = Pattern.compile("Qty\\s*[:：]?\\s*(\\d+)").matcher(text);
        if (qty.find()) excelViewModel.setQuantity(qty.group(1));

        Matcher priceMatcher = Pattern.compile("([0-9]+(?:\\.[0-9]{3})*(?:,[0-9]{2}))").matcher(text);
        if (priceMatcher.find()) excelViewModel.setUnitPrice(priceMatcher.group(1));
        return excelViewModel;
    }

    public String generateExcelFromPdf(MultipartFile file) throws Exception {

        List<ExcelViewModel> list = new ArrayList<>();
        List<BufferedImage> images = renderAllPagesAsImages(file);

        //Her bir pdf sayfası için çalışır
        for (BufferedImage image : images) {
            //imajı okur ve texte çevirir
            String ocrText = extractTextWithOCR(image);
            //İstediğimiz değerleri textten extract edip classa mapler
            ExcelViewModel excelViewModel = extractFieldsFromText(ocrText);
            //excel satırını listeye ekler
            list.add(excelViewModel);
        }

        //geriye excel dosyasının yerini döner
        return excelService.generateExcel(list);
    }


}
