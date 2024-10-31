package com.lingsida.qrrecepit;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
public class QRCodeExtractorService {

    @Value("${qrcode.directory}")
    private String directory;

    public QRCodeExtractorService() {
        // 默认构造函数
    }

    public String extractQRCode(String pdfPath) throws IOException {
        try (PDDocument document = PDDocument.load(new File(pdfPath))) {
            PDFRenderer pdfRenderer = new PDFRenderer(document);
            BufferedImage image = pdfRenderer.renderImageWithDPI(0, 300);
            BufferedImage croppedImage = image.getSubimage(0, 0, (int) (image.getWidth() * 0.2), (int) (image.getHeight() * 0.3));
            LuminanceSource source = new BufferedImageLuminanceSource(croppedImage);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

            // 使用解码提示
            Map<DecodeHintType, Object> hints = new EnumMap<>(DecodeHintType.class);
            hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);

            Result result = new QRCodeReader().decode(bitmap, hints);
            return result.getText();
        } catch (NotFoundException | ChecksumException | FormatException e) {
            // 处理找不到二维码或校验和错误的情况
            System.err.println("Error decoding QR code: " + e.getMessage());
            return null;
        }
    }

    public List<String[]> processPDFs() throws IOException {
        List<String[]> data = new ArrayList<>();
        File dir = new File(directory);
        for (File file : dir.listFiles()) {
            if (file.getName().endsWith(".pdf")) {
                System.out.println("Processing " + file.getName());
                String qrContent = extractQRCode(file.getAbsolutePath());
                String[] fields = qrContent != null ? qrContent.split(",") : new String[7];
                data.add(concat(new String[]{file.getName()}, fields));
                System.out.println("QR码内容: " + qrContent);
            }
        }
        return data;
    }

    public void saveToExcel(List<String[]> data, String outputFile) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Sheet1");
        String[] columns = {"文件名", "属性1", "发票种类代码", "发票代码", "发票号码", "开票金额", "开票日期", "发票校验码", "机密信息"};
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < columns.length; i++) {
            headerRow.createCell(i).setCellValue(columns[i]);
        }
        int rowNum = 1;
        for (String[] record : data) {
            Row row = sheet.createRow(rowNum++);
            for (int i = 0; i < record.length; i++) {
                row.createCell(i).setCellValue(record[i]);
            }
        }
        try (FileOutputStream fileOut = new FileOutputStream(outputFile)) {
            workbook.write(fileOut);
        }
        workbook.close();
    }

    private String[] concat(String[] first, String[] second) {
        String[] result = new String[first.length + second.length];
        System.arraycopy(first, 0, result, 0, first.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }
}
