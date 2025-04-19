package com.pera.excelgenerator.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
public class FileDeleteScheduler {

    private static final String DIRECTORY_PATH = System.getProperty("user.dir")+"/excelFiles";

    @Scheduled(cron = "0 0 3 * * *")
    public void cleanUpExcelFiles() {
        File directory = new File(DIRECTORY_PATH);
        if (!directory.exists() || !directory.isDirectory()) {
            System.out.println("[Cleanup] Klasör bulunamadı: " + DIRECTORY_PATH);
            return;
        }

        File[] files = directory.listFiles();
        if (files == null || files.length == 0) {
            System.out.println("[Cleanup] Silinecek dosya yok.");
            return;
        }

        for (File file : files) {
            if (file.isFile()) {
                boolean deleted = file.delete();
                System.out.println("[Cleanup] " + file.getName() + (deleted ? " silindi." : " silinemedi!"));
            }
        }
    }
}
