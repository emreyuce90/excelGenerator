package com.pera.excelgenerator.controller;

import com.pera.excelgenerator.services.ExcelService;
import com.pera.excelgenerator.services.OcrService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class FileController {

    private final OcrService ocrService;
    private final ExcelService excelService;

    @PostMapping("/pdfToExcel")
    public String extractFields(@RequestParam("file") MultipartFile file) {
        try {
            return ocrService.generateExcelFromPdf(file);

        } catch (Exception e) {
            System.out.println(e.getMessage());
            return e.getMessage();
        }
    }
}
