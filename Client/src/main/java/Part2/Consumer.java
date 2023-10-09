package Part2;

import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;

public class Consumer implements Runnable {
    private final int numThreads;
    private final BlockingQueue<ArrayList<String>> resultsBuffer;
    private final WriteToCsv writeToCsv;
    private final String fileName;
    private final String sheetName;

    public Consumer(int numThreads, BlockingQueue<ArrayList<String>> resultsBuffer, String fileName, String sheetName) {
        this.numThreads = numThreads;
        this.resultsBuffer = resultsBuffer;
        this.writeToCsv = new WriteToCsv();
        this.fileName = fileName;
        this.sheetName = sheetName;
    }

    @Override
    public void run() {
        for (int i = 0; i < this.numThreads; i++){
            try {
                ArrayList<String> groupResults = this.resultsBuffer.take(); // Blocks until an item is available
                writeToCsv.writeLoadTestResultsToSheet(this.fileName, this.sheetName, groupResults);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
