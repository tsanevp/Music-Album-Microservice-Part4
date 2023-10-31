package Client;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.Thread.sleep;

public class AlbumClient {
    private static final int INITIAL_THREAD_COUNT = 10;
    private static final int INITIAL_CALLS_PER_THREAD = 100;
    protected static final AtomicInteger SUCCESSFUL_REQ = new AtomicInteger(0);
    protected static final AtomicInteger FAILED_REQ = new AtomicInteger(0);
    protected static List<Long> latenciesPost = Collections.synchronizedList(new ArrayList<>());
    protected static List<Long> latenciesGet = Collections.synchronizedList(new ArrayList<>());
    protected static CountDownLatch totalThreadsLatch;

    public static void main(String[] args) throws InterruptedException {
        long start, end;
        String currentPhase = "Loading Java Server Phase - App Load Balancer - 3 Servlets (t2.medium), 1 DB (db.t3.medium, 225 Connections)";

        // Define starting constants
        int threadGroupSize = Integer.parseInt(args[0]);
        int numThreadGroups = Integer.parseInt(args[1]);
        long delay = Long.parseLong(args[2]);
        String serverURL = args[3];

        // Thread calls and calculations
        int callsPerThread = 1000;
        int maxThreads = threadGroupSize * numThreadGroups;

        // Executor service used for thread pooling and countdown latch to track when loading is complete
        ExecutorService servicePool = Executors.newFixedThreadPool(maxThreads);
        totalThreadsLatch = new CountDownLatch(INITIAL_THREAD_COUNT);

        // Run initialization phase
//        start = System.currentTimeMillis();
        initializationPhase(servicePool, serverURL);
//        end = System.currentTimeMillis();
//        printResults(1, INITIAL_THREAD_COUNT, INITIAL_CALLS_PER_THREAD, "Initialization Phase Results", INITIAL_THREAD_COUNT * INITIAL_CALLS_PER_THREAD * 2, INITIAL_THREAD_COUNT, start, end);

        // Redefine countdown latch for server loading phase
        totalThreadsLatch = new CountDownLatch(maxThreads);

        // Load Server
        start = System.currentTimeMillis();
        loadServerPhase(numThreadGroups, threadGroupSize, delay, serverURL, callsPerThread, servicePool);
        end = System.currentTimeMillis();

        printResults(numThreadGroups, threadGroupSize, callsPerThread, currentPhase, start, end);
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
                servicePool.execute(new AlbumThreadRunnable(callsPerThread, serverURL, false));
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
            servicePool.execute(new AlbumThreadRunnable(INITIAL_CALLS_PER_THREAD, serverURL, true));
        }
        totalThreadsLatch.await();
    }

    /**
     * Simple method to print the results of the initialization phase and loading phase to the CL.
     *
     * @param numThreadGroups - The number of thread groups ran.
     * @param threadGroupSize - The number of threads created in each group.
     * @param callsPerThread  - The number of GET and POST requests each thread makes.
     * @param currentPhase    - The name of the current loading phase.
     * @param end             - The end time of the current phase.
     * @param start           - The start time of the current phase.
     */
    protected static void printResults(int numThreadGroups, int threadGroupSize, int callsPerThread, String currentPhase, long start, long end) {
        LoadCalculations loadCalculationsPost = new LoadCalculations(latenciesPost);
        LoadCalculations loadCalculationsGet = new LoadCalculations(latenciesGet);

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
        System.out.println("-- POST Requests --");
        printCalculations(decimalFormat, loadCalculationsPost);
        System.out.println("-- GET Requests --");
        printCalculations(decimalFormat, loadCalculationsGet);
        System.out.println("-------- End of Results For " + currentPhase + " --------");
        System.out.println("-----------------------------------------------------------------------------------------");
    }

    /**
     * Print out the calculations for the request type.
     *
     * @param decimalFormat    - The decimal format to print the results.
     * @param loadCalculations - The results for either POST or GET requests.
     */
    private static void printCalculations(DecimalFormat decimalFormat, LoadCalculations loadCalculations) {
        System.out.println("Mean Response Time: " + decimalFormat.format(loadCalculations.getMeanResponseTime()) + " (ms)");
        System.out.println("Median Response Time: " + decimalFormat.format(loadCalculations.getMedianResponseTime()) + " (ms)");
        System.out.println("p99 Response Time: " + decimalFormat.format(loadCalculations.getPercentile(99)) + " (ms)");
        System.out.println("Min Response Time: " + decimalFormat.format(loadCalculations.getMin()) + " (ms)");
        System.out.println("Max Response Time: " + decimalFormat.format(loadCalculations.getMax()) + " (ms)");
    }
}