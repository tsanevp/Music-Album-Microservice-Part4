package Part2;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static java.lang.Thread.sleep;

public class AlbumClient {
    private static final int INITIAL_THREAD_COUNT = 10;
    private static final int INITIAL_CALLS_PER_THREAD = 100;
    protected static final AtomicInteger SUCCESSFUL_REQ = new AtomicInteger(0);
    protected static final AtomicInteger FAILED_REQ = new AtomicInteger(0);

    protected static final AtomicLong SUM_LATENCY_EACH_REQ = new AtomicLong(0);
    protected static List<Long> latencies = Collections.synchronizedList(new ArrayList<>());
    protected static CountDownLatch totalThreadsLatch;
//    protected static WriteToCsv writeToCsv;

    public static void main(String[] args) throws InterruptedException {
        // Create new sheet for current test in results csv file
        String sheetName = "Go-TG20-T1";
        String fileName = "Go10Threads3";

//        CountDownLatch sheetCountDownLatch = new CountDownLatch(1);
//        writeToCsv = new WriteToCsv(fileName, sheetName, sheetCountDownLatch);
//        sheetCountDownLatch.await();

        long start, end;
        int testNum = 1;
        String currentPhase = "Loading Java Server Phase (Test #" + testNum + ")";

        // Define starting constants
        int threadGroupSize = 10;
        int numThreadGroups = 10;
        long delay = 2;

        // EC2 Server
//        String serverURL = "http://CS6650-network-lb-acaafa34b4ad7371.elb.us-west-2.amazonaws.com/Server_Web"; // mysql
        String serverURL = "http://localhost:8080/Server_Web_exploded"; // mysql

        // Thread calls and calculations
        int callsPerThread = 1000;
        int maxThreads = threadGroupSize * numThreadGroups;
        int totalCalls = maxThreads * callsPerThread * 2;

        // Executor service used for thread pooling and countdown latch to track when loading is complete
        ExecutorService servicePool = Executors.newFixedThreadPool(maxThreads);
        totalThreadsLatch = new CountDownLatch(INITIAL_THREAD_COUNT);

        // Run initialization phase
        start = System.currentTimeMillis();
        initializationPhase(servicePool, serverURL);
        end = System.currentTimeMillis();
        printResults(1, INITIAL_THREAD_COUNT, INITIAL_CALLS_PER_THREAD, "Initialization Phase Results", INITIAL_THREAD_COUNT * INITIAL_CALLS_PER_THREAD * 2, INITIAL_THREAD_COUNT, start, end);

        // Redefining tracking variables for server loading phase
        totalThreadsLatch = new CountDownLatch(maxThreads);
        SUCCESSFUL_REQ.set(0);
        FAILED_REQ.set(0);
        SUM_LATENCY_EACH_REQ.set(0);

        // Load Server
        start = System.currentTimeMillis();
        loadServerPhase(numThreadGroups, threadGroupSize, delay, serverURL, callsPerThread, servicePool);
        end = System.currentTimeMillis();


//        writeToCsv.writeLoadTestResultsToSheet();
        printResults(numThreadGroups, threadGroupSize, callsPerThread, currentPhase, totalCalls, maxThreads, start, end);
    }

    /**
     * Method to run server loading phase. For number of groups, create a number of threads that each make a predefined
     * amount of POST & GET requests.
     *
     * @param numThreadGroups - The number of thread groups to create.
     * @param threadGroupSize - The number of threads to create in each group.
     * @param delay           - The delay the main thread should wait for between each thread group execution.
     * @param serverURL       - The server url each request should target.
     * @param callsPerThread  - The amount of requests each POST & GET methods should make in a single thread.
     * @param servicePool     - The executor service pool to create and startup threads from.
     * @throws InterruptedException - Is thrown if the waiting thread is interrupted while waiting for the latch.
     */
    private static void loadServerPhase(int numThreadGroups, int threadGroupSize, long delay, String serverURL, int callsPerThread, ExecutorService servicePool) throws InterruptedException {
        for (int i = 0; i < numThreadGroups; i++) {
            for (int j = 0; j < threadGroupSize; j++) {
                servicePool.execute(new AlbumThreadRunnable(callsPerThread, serverURL, true));
            }

            // Sleep for delay amount of time, converted to seconds
            sleep(delay * 1000L);
        }

        // Shutdown the executor and wait for all tasks to complete
        totalThreadsLatch.await();
        servicePool.shutdown();
    }

    /**
     * Method to run initialization phase before loading server.
     *
     * @param servicePool - The executor service thread pool that executes the thread group runnable threads.
     * @param serverURL   - The server url the client should make a request to.
     * @throws InterruptedException - Is thrown if the waiting thread is interrupted while waiting for the latch.
     */
    private static void initializationPhase(ExecutorService servicePool, String serverURL) throws InterruptedException {
        for (int i = 0; i < INITIAL_THREAD_COUNT; i++) {
            servicePool.execute(new AlbumThreadRunnable(INITIAL_CALLS_PER_THREAD, serverURL, false));
        }
        totalThreadsLatch.await();
    }

    /**
     * Simple method to print the results of the initialization phase and loading phase to the CL.
     *
     * @param numThreadGroups - The number of thread groups ran.
     * @param threadGroupSize - The number of threads created in each group.
     * @param callsPerThread  - The number of GET and POST requests each thread makes.
     * @param totalCalls      - The total requests made to the api.
     * @param maxThreads      - The maximum amount of threads that could be running at once.
     * @param end             - The end time of the current phase.
     * @param start           - The start time of the current phase.
     */
    protected static void printResults(int numThreadGroups, int threadGroupSize, int callsPerThread, String currentPhase, int totalCalls, int maxThreads, long start, long end) {
        LoadCalculations loadCalculations = new LoadCalculations(latencies);
        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        double wallTime = (end - start) * 0.001;

        System.out.println("-----------------------------------------------------------------------------------------");
        System.out.println("-------- Printing Results For " + currentPhase + " --------");
        System.out.println("Thread Groups = " + numThreadGroups + ", Number of Threads per Group = " + threadGroupSize + ", Call per Thread = " + callsPerThread);
        System.out.println("Number of Successful Requests: " + SUCCESSFUL_REQ.get());
        System.out.println("Number of Failed Requests: " + FAILED_REQ.get() + "\n");
        System.out.println("-------- Results --------");
        System.out.println("---- Throughput's & Wall Time ----");
        System.out.println("Throughput: " + decimalFormat.format(SUCCESSFUL_REQ.get() / wallTime) + " (req/sec) ---> total successful requests / wall time");
        System.out.println("Wall Time: " + decimalFormat.format(wallTime) + " (sec)\n");
        System.out.println("---- Calculations ----");
        System.out.println("Mean Response Time: " + decimalFormat.format(loadCalculations.getMeanResponseTime()) + " (ms)");
        System.out.println("Median Response Time: " + decimalFormat.format(loadCalculations.getMedianResponseTime()) + " (ms)");
        System.out.println("p99 Response Time: " + decimalFormat.format(loadCalculations.getPercentile(99)) + " (ms)");
        System.out.println("Min Response Time: " + decimalFormat.format(loadCalculations.getMin()) + " (ms)");
        System.out.println("Max Response Time: " + decimalFormat.format(loadCalculations.getMax()) + " (ms)");
        System.out.println("-------- End of Results For " + currentPhase + " --------");
        System.out.println("-----------------------------------------------------------------------------------------");
    }
}