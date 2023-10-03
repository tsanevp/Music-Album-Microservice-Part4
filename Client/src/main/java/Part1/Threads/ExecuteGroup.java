package Part1.Threads;

import io.swagger.client.ApiClient;
import io.swagger.client.api.DefaultApi;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExecuteGroup {
    private final BlockingQueue<String> buffer;
    private final ExecutorService threadPool;
    private final int threadGroupSize;
    private final int callsPerThread;
    private final String serverURL;
    public ExecuteGroup(BlockingQueue<String> buffer, ExecutorService threadPool, int threadGroupSize, int callsPerThread, String serverURL) {
        this.buffer = buffer;
        this.threadPool = threadPool;
        this.threadGroupSize = threadGroupSize;
        this.callsPerThread = callsPerThread;
        this.serverURL = serverURL;
    }

    public void runStuff() {
        DefaultApi api = initializeApiClient();
        CountDownLatch countThreadGroup = new CountDownLatch(this.threadGroupSize * this.callsPerThread);
        this.threadPool.execute(new Producer(this.buffer, this.callsPerThread));
        System.out.println("hello");
    }

    private DefaultApi initializeApiClient() {
        ApiClient apiClient = new ApiClient();
        apiClient.setBasePath(this.serverURL);
        return new DefaultApi(apiClient);
    }
}
