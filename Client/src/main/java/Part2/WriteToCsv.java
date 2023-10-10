package Part2;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class WriteToCsv {

  private final String fileName;
  private final String sheetName;
  private final CountDownLatch countDownLatch;
  protected final List<String[]> loadResults;

  public WriteToCsv(String fileName, String sheetName, CountDownLatch countDownLatch) {
    this.fileName = fileName;
    this.sheetName = sheetName;
    this.countDownLatch = countDownLatch;
    this.loadResults = new ArrayList<>();
    this.createNewSheetForLoadTest();
  }

  protected synchronized void addThreadResults(List<String[]> threadResults) {
    this.loadResults.addAll(threadResults);
  }
  protected void createNewSheetForLoadTest() {
    String filePath = "src/main/java/Part2/" + this.fileName + ".xlsx";

    try {
      FileInputStream fileInputStream = new FileInputStream(filePath);
      Workbook workbook = new XSSFWorkbook(fileInputStream);

      Sheet sheet = workbook.createSheet(this.sheetName);

      Row headers = sheet.createRow(0);

      headers.createCell(0).setCellValue("Start Time (ms)");
      headers.createCell(1).setCellValue("Request Type (POST/GET)");
      headers.createCell(2).setCellValue("Latency (ms)");
      headers.createCell(3).setCellValue("Response Code");

      FileOutputStream outputStream = new FileOutputStream(filePath);
      workbook.write(outputStream);

      outputStream.close();
      fileInputStream.close();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    this.countDownLatch.countDown();
  }

  protected synchronized void writeLoadTestResultsToSheet() {
    String filePath = "src/main/java/Part2/" + this.fileName + ".xlsx";

    try {
      FileInputStream fileInputStream = new FileInputStream(filePath);
      Workbook workbook = new XSSFWorkbook(fileInputStream);

      Sheet sheet = workbook.getSheet(this.sheetName);

      int newRowNum = sheet.getLastRowNum() + 1;

      for (String[] result : this.loadResults) {
        Row newRow = sheet.createRow(newRowNum);
        newRow.createCell(0).setCellValue(result[0]);
        newRow.createCell(1).setCellValue(result[1]);
        newRow.createCell(2).setCellValue(result[2]);
        newRow.createCell(3).setCellValue(result[3]);

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
