package com.pera.excelgenerator.services;

import models.ExcelViewModel;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Service
public class ExcelService {

    public String generateExcel(List<ExcelViewModel> excelViewModels) throws IOException {
        Workbook workbook = new XSSFWorkbook();

        Sheet sheet = workbook.createSheet("Data");

        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("MAL KODU");
        headerRow.createCell(1).setCellValue("ADET");
        headerRow.createCell(2).setCellValue("MİKTAR CİNSİ");
        headerRow.createCell(3).setCellValue("BOŞ");
        headerRow.createCell(4).setCellValue("KIYMET1");
        headerRow.createCell(5).setCellValue("KIYMET2");
        headerRow.createCell(6).setCellValue("GTIP");
        headerRow.createCell(7).setCellValue("MENŞE");
        headerRow.createCell(8).setCellValue("BRÜT");
        headerRow.createCell(9).setCellValue("NET");
        headerRow.createCell(10).setCellValue("TİCARİ TANIM");
        headerRow.createCell(11).setCellValue("ANTREPO NO");
        headerRow.createCell(12).setCellValue("ANTREPO SIRA");
        headerRow.createCell(13).setCellValue("KULLANILMIŞ");
        headerRow.createCell(14).setCellValue("ATR-DIGER");
        headerRow.createCell(15).setCellValue("KAP ADEDI");
        headerRow.createCell(16).setCellValue("KAP CİNSİ");
        headerRow.createCell(17).setCellValue("MALIN CİNSİ");
        headerRow.createCell(18).setCellValue("MARKA");
        headerRow.createCell(19).setCellValue("NO");
        headerRow.createCell(20).setCellValue("CIF DIGER TUTAR");

        int rowNum = 1;
        for(ExcelViewModel excelViewModel : excelViewModels){
            Row row = sheet.createRow(rowNum++);
            row.createCell(1).setCellValue(excelViewModel.getQuantity());
            row.createCell(2).setCellValue(excelViewModel.getProductCode());
            row.createCell(4).setCellValue(excelViewModel.getUnitPrice());
            row.createCell(5).setCellValue(excelViewModel.getTotalAmount());
            row.createCell(6).setCellValue(excelViewModel.getGtip());
            row.createCell(7).setCellValue(excelViewModel.getOrigin());
            row.createCell(8).setCellValue(excelViewModel.getGrossWeigth());
            row.createCell(9).setCellValue(excelViewModel.getNetWeigth());
            row.createCell(10).setCellValue(excelViewModel.getProductName());
        }

        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

        String projectDir = System.getProperty("user.dir");

        String filePath = projectDir + "/excelFiles/" + timestamp + ".xlsx";

        try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
            workbook.write(fileOut);
        }

        workbook.close();
        return filePath;
    }
}
