package Part2;

import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;

public class Consumer implements Runnable {
    private final BlockingQueue<ArrayList<String>> resultsBuffer;
    private final WriteToCsv writeToCsv;
    private final String fileName;
    private final String sheetName;

    public Consumer(BlockingQueue<ArrayList<String>> resultsBuffer, String fileName, String sheetName) {
        this.resultsBuffer = resultsBuffer;
        this.writeToCsv = new WriteToCsv();
        this.fileName = fileName;
        this.sheetName = sheetName;
    }

    @Override
    public void run() {
        synchronized (this.resultsBuffer) {
            while (this.resultsBuffer.isEmpty()) {
                try {
                    this.resultsBuffer.wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

            ArrayList<String> groupResults = this.resultsBuffer.poll();
            writeToCsv.writeLoadTestResultsToSheet(fileName, sheetName, groupResults);
        }
    }
}
