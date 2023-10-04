package Part1;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;

public class WriteToCsv {

    private final String testType;
    private final int numThreads;
    private final int numCalls;
    private final int successfulReq;
    private final int failedReq;
    private final double avgTimeReq;
    private final double throughPut;
    private final double wallTime;

    public WriteToCsv(String testType, int numThreads, int numCalls, int successfulReq, int failedReq, double avgTimeReq, double throughPut, double wallTime) {
        this.testType = testType;
        this.numThreads = numThreads;
        this.numCalls = numCalls;
        this.successfulReq = successfulReq;
        this.failedReq = failedReq;
        this.avgTimeReq = avgTimeReq;
        this.throughPut = throughPut;
        this.wallTime = wallTime;
    }

    protected void writeTestResults() {
        try {
            FileInputStream fileInputStream = new FileInputStream("src/main/java/TestingResults.xlsx");
            Workbook workbook = new XSSFWorkbook(fileInputStream);
            Sheet sheet = workbook.getSheetAt(0);

            int newRowNum = sheet.getLastRowNum() + 1;

            Row newRow = sheet.createRow(newRowNum);

            newRow.createCell(0).setCellValue(this.testType);
            newRow.createCell(1).setCellValue(this.numThreads);
            newRow.createCell(2).setCellValue(this.numCalls);
            newRow.createCell(3).setCellValue(this.successfulReq);
            newRow.createCell(4).setCellValue(this.failedReq);
            newRow.createCell(5).setCellValue(this.avgTimeReq);
            newRow.createCell(6).setCellValue(this.throughPut);
            newRow.createCell(7).setCellValue(this.wallTime);

            FileOutputStream outputStream = new FileOutputStream("src/main/java/TestingResults.xlsx");
            workbook.write(outputStream);

            outputStream.close();
            fileInputStream.close();

            System.out.println("Added results from " + testType + " test.");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
