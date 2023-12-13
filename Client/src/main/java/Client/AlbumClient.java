package Client;

import Util.Constants;
import Util.LoadCalculations;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.Thread.sleep;

public class AlbumClient {
    protected static final AtomicInteger SUCCESSFUL_REQ = new AtomicInteger(0);
    protected static final AtomicInteger FAILED_REQ = new AtomicInteger(0);
    protected static final AtomicInteger SUCCESSFUL_GET_REQ = new AtomicInteger(0);
    protected static final AtomicInteger FAILED_GET_REQ = new AtomicInteger(0);
    protected static boolean MAKE_GET_REQS = false;
    protected static List<Long> albumPost = Collections.synchronizedList(new ArrayList<>());
    protected static List<Long> likesPost = Collections.synchronizedList(new ArrayList<>());
    protected static List<Long> dislikesPost = Collections.synchronizedList(new ArrayList<>());
    protected static List<Long> reviewGet = Collections.synchronizedList(new ArrayList<>());
    protected static BlockingQueue<String> albumIdsToCall = new LinkedBlockingQueue<>();

    protected static CountDownLatch totalThreadsLatch;
    protected static CountDownLatch getReqThreadsLatch;
    protected static CountDownLatch getReqWaitToStartLatch;

    protected static long startGETReqs;

    public static void main(String[] args) throws InterruptedException {
        long start, end;
        String currentPhase = "Loading Java Server Phase";

        // Define starting constants
        int threadGroupSize = Integer.parseInt(args[0]);
        int numThreadGroups = Integer.parseInt(args[1]);
        long delay = Long.parseLong(args[2]);
        String serverURL = args[3];
        String getServerURL = args[4];

        // Max thread count
        int maxThreads = threadGroupSize * numThreadGroups;

        // Executor service used for thread pooling and countdown latch to track when loading is complete
        ExecutorService servicePool = Executors.newFixedThreadPool(maxThreads);
        totalThreadsLatch = new CountDownLatch(Constants.INITIAL_THREAD_COUNT);

        // Executor service used to make continuous GET requests
        ExecutorService getReqServicePool = Executors.newFixedThreadPool(Constants.NUM_THREADS_FOR_GET_REQS);
        getReqThreadsLatch = new CountDownLatch(Constants.NUM_THREADS_FOR_GET_REQS);
        getReqWaitToStartLatch = new CountDownLatch(1); // Used to signal first thread group has completed

        // Start GET req threads. They will not make reqs until first group has completed
        for (int j = 0; j < Constants.NUM_THREADS_FOR_GET_REQS; j++) {
            getReqServicePool.execute(new GetAlbumRunnable(getServerURL));
        }

        // Run initialization phase
        initializationPhase(servicePool, serverURL);

        // Redefine countdown latch for loading phase
        totalThreadsLatch = new CountDownLatch(maxThreads);

        // Load Server phase
        start = System.currentTimeMillis();
        loadServerPhase(numThreadGroups, threadGroupSize, delay, serverURL, servicePool, getReqServicePool, getServerURL);
        end = System.currentTimeMillis();

        printResults(numThreadGroups, threadGroupSize, currentPhase, start, end);
    }

    /**
     * Method to run server loading phase. For number of groups, create a number of threads that each make a predefined
     * amount of POST & GET requests.
     *
     * @param numThreadGroups - The number of thread groups to create.
     * @param threadGroupSize - The number of threads to create in each group.
     * @param delay           - The delay the main thread should wait for between each thread group execution.
     * @param serverURL       - The server url each request should target.
     * @param servicePool     - The executor service pool to create and startup threads from.
     * @throws InterruptedException - Is thrown if the waiting thread is interrupted while waiting for the latch.
     */
    private static void loadServerPhase(int numThreadGroups, int threadGroupSize, long delay, String serverURL, ExecutorService servicePool, ExecutorService getReqServicePool, String getServerURL) throws InterruptedException {
        for (int i = 0; i < numThreadGroups; i++) {
            for (int j = 0; j < threadGroupSize; j++) {
                servicePool.execute(new AlbumThreadRunnable(Constants.CALLS_PER_THREAD, serverURL, false));
            }

            // Sleep for delay amount of time, converted to seconds
            sleep(delay * 1000L);

            // Start get reqs after first group
            if (i == 0) {
                AlbumClient.getReqWaitToStartLatch.countDown();
                AlbumClient.startGETReqs = System.currentTimeMillis();
                AlbumClient.MAKE_GET_REQS = true;
            }
        }

        // Shutdown the executor and wait for all tasks to complete
        totalThreadsLatch.await();
        MAKE_GET_REQS = false;

        servicePool.shutdown();
        getReqThreadsLatch.await();
        getReqServicePool.shutdown();
    }

    /**
     * Method to run initialization phase before loading server.
     *
     * @param servicePool - The executor service thread pool that executes the thread group runnable threads.
     * @param serverURL   - The server url the client should make a request to.
     * @throws InterruptedException - Is thrown if the waiting thread is interrupted while waiting for the latch.
     */
    private static void initializationPhase(ExecutorService servicePool, String serverURL) throws InterruptedException {
        for (int i = 0; i < Constants.INITIAL_THREAD_COUNT; i++) {
            servicePool.execute(new AlbumThreadRunnable(Constants.INITIAL_CALLS_PER_THREAD, serverURL, true));
        }
        totalThreadsLatch.await();
    }

    /**
     * Simple method to print the results of the initialization phase and loading phase to the CL.
     *
     * @param numThreadGroups - The number of thread groups ran.
     * @param threadGroupSize - The number of threads created in each group.
     * @param currentPhase    - The name of the current loading phase.
     * @param start           - The start time of the current phase.
     * @param end             - The end time of the current phase.
     */
    protected static void printResults(int numThreadGroups, int threadGroupSize, String currentPhase, long start, long end) {
        // Get load results for all req types
        LoadCalculations loadCalculationsAlbumsPost = new LoadCalculations(albumPost);
        LoadCalculations loadCalculationsLikesPost = new LoadCalculations(likesPost);
        LoadCalculations loadCalculationsDislikesPost = new LoadCalculations(dislikesPost);
        LoadCalculations loadCalculationsGetReviews = new LoadCalculations(reviewGet);

        // Define format and wall time
        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        double wallTime = (end - start) * 0.001;

        // Overall results
        System.out.println("-----------------------------------------------------------------------------------------");
        System.out.println("-------- Printing Results For " + currentPhase + " --------");
        System.out.println("Thread Groups = " + numThreadGroups + ", Number of Threads per Group = " + threadGroupSize + ", Call per Thread = " + Constants.CALLS_PER_THREAD);
        System.out.println("Number of Successful Requests: " + SUCCESSFUL_REQ.get());
        System.out.println("Number of Failed Requests: " + FAILED_REQ.get() + "\n");
        System.out.println("-------- Results --------");
        System.out.println("---- Throughput's & Wall Time ----");
        System.out.println("Throughput: " + decimalFormat.format(SUCCESSFUL_REQ.get() / wallTime) + " (req/sec) ---> total successful requests / wall time");
        System.out.println("Wall Time: " + decimalFormat.format(wallTime) + " (sec)\n");
        System.out.println("---- Calculations ----");
        System.out.println("-- Album POST Requests --");
        printCalculations(decimalFormat, loadCalculationsAlbumsPost);
        System.out.println("-- Like POST Requests --");
        printCalculations(decimalFormat, loadCalculationsLikesPost);
        System.out.println("-- Dislike POST Requests --");
        printCalculations(decimalFormat, loadCalculationsDislikesPost);

        // GET review req results
        System.out.println("-------- Printing Results For GET Reviews --------");
        System.out.println("Number of Successful Requests: " + SUCCESSFUL_GET_REQ.get());
        System.out.println("Number of Failed Requests: " + FAILED_GET_REQ.get() + "\n");
        System.out.println("-------- Results --------");
        System.out.println("---- Throughput's & Wall Time ----");
        wallTime = (end - startGETReqs) * 0.001;

        System.out.println("Throughput: " + decimalFormat.format(SUCCESSFUL_GET_REQ.get() / wallTime) + " (req/sec) ---> total successful requests / wall time");
        System.out.println("Wall Time: " + decimalFormat.format(wallTime) + " (sec)\n");
        System.out.println("---- Calculations ----");
        System.out.println("-- GET Requests --");
        printCalculations(decimalFormat, loadCalculationsGetReviews);
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