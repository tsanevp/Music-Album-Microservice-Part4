package Part1.Threads;

import java.util.concurrent.BlockingQueue;

public class Producer implements Runnable {

    private final BlockingQueue<String> buffer;
    private final int numApiCalls;


    public Producer(BlockingQueue<String> buffer, int numApiCalls) {
        this.buffer = buffer;
        this.numApiCalls = numApiCalls;
    }

    /**
     * Generate GET & POST request information and store in buffer.
     *
     * @throws InterruptedException throws exception while waiting.
     */
    private void generateRequests() throws InterruptedException {
        buffer.put("POST");
        buffer.put("GET");
    }

    @Override
    public void run() {
        try {
            for (int i = 0; i < numApiCalls; i++){
                generateRequests();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

}
