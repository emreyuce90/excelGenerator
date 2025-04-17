package com.pera.excelgenerator.controller;

import com.pera.excelgenerator.services.ExcelService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.awt.image.BufferedImage;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class PdfController {
    
    private final ExcelService excelService;

    @PostMapping("/pdf")
    public String getExcelFile(@RequestParam("file") MultipartFile file) throws Exception {
        String s = excelService.extractGtipNumber(file);
        return null;
    }

    @PostMapping("/ocr")
    public ResponseEntity<String> getOcrText(@RequestParam("file") MultipartFile file) {
        try {
            BufferedImage image = excelService.renderFirstPageAsImage(file);
            String ocrText = excelService.extractTextWithOCR(image);
            return ResponseEntity.ok(ocrText);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("OCR Hatası: " + e.getMessage());
        }
    }

    @PostMapping("/fields")
    public ResponseEntity<Map<String, String>> extractFields(@RequestParam("file") MultipartFile file) {
        try {
            BufferedImage image = excelService.renderFirstPageAsImage(file);
            String ocrText = excelService.extractTextWithOCR(image);
            Map<String, String> fields = excelService.extractInvoiceFieldsFromText(ocrText);
            return ResponseEntity.ok(fields);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }


}
