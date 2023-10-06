package Part2;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class WriteToCsv {
    public WriteToCsv() {
    }

    protected void createNewSheetForLoadTest(String fileName, String newSheetName) {
        String filePath = "src/main/java/Part2/" + fileName + ".xlsx";

        try {
            FileInputStream fileInputStream = new FileInputStream(filePath);
            Workbook workbook = new XSSFWorkbook(fileInputStream);

            Sheet sheet = workbook.createSheet(newSheetName);

            Row headers = sheet.getRow(0);

            headers.createCell(0).setCellValue("Start Time (ms)");
            headers.createCell(1).setCellValue("Request Type (POST/GET)");
            headers.createCell(2).setCellValue("Latency (ms)");
            headers.createCell(3).setCellValue("Response Code");

            FileOutputStream outputStream = new FileOutputStream("src/main/java/TestingResults.xlsx");
            workbook.write(outputStream);

            outputStream.close();
            fileInputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected void writeLoadTestResultsToSheet(String fileName, String sheetNameToWriteResults, ArrayList<String> resultsToUpload) {
        String filePath = "src/main/java/Part2/" + fileName + ".xlsx";
        System.out.println("hello");
        try {
            FileInputStream fileInputStream = new FileInputStream(filePath);
            Workbook workbook = new XSSFWorkbook(fileInputStream);

            Sheet sheet = workbook.getSheet(sheetNameToWriteResults);

            int newRowNum = sheet.getLastRowNum() + 1;

            for (String result : resultsToUpload) {
                String[] current = result.split(",");

                Row newRow = sheet.createRow(newRowNum);
                newRow.createCell(0).setCellValue(current[0]);
                newRow.createCell(1).setCellValue(current[1]);
                newRow.createCell(2).setCellValue(current[2]);
                newRow.createCell(3).setCellValue(current[3]);

                newRowNum++;
            }

            FileOutputStream outputStream = new FileOutputStream(filePath);
            workbook.write(outputStream);

            outputStream.close();
            fileInputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
