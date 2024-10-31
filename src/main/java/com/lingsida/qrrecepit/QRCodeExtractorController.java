package com.lingsida.qrrecepit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class QRCodeExtractorController {

    @Autowired
    private QRCodeExtractorService qrCodeExtractorService;

    @GetMapping("/extract")
    public String extractQRCodes() {
        try {
            List<String[]> data = qrCodeExtractorService.processPDFs();
            qrCodeExtractorService.saveToExcel(data, "output.xlsx");
            return "数据已保存到 output.xlsx";
        } catch (Exception e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }
}
