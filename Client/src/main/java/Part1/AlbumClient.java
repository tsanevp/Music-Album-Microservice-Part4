package Part1;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static java.lang.Thread.sleep;

public class AlbumClient {

//    protected static final AtomicInteger SUCCESSFUL_REQ = new AtomicInteger(0);
//    protected static final AtomicInteger FAILED_REQ = new AtomicInteger(0);
//    protected static final AtomicLong TIME_EACH_REQUEST = new AtomicLong(0);
    protected static CountDownLatch totalThreadsLatch;


    private static final int INITIAL_THREAD_COUNT = 10;
    private static final int INITIAL_CALLS_PER_THREAD = 100;

    public AlbumClient() {
    }

    public static void main(String[] args) throws InterruptedException {
//        int callsPerThread = 1000;
//
//        int threadGroupSize = Integer.parseInt(args[0]);
//        int numThreadGroups = Integer.parseInt(args[1]);
//        long delay = Integer.parseInt(args[2]);
//        String serverURL = String.valueOf(Integer.parseInt(args[3]));

        // Define initial arguments

        // Define starting constants
        int threadGroupSize = 10;
        int numThreadGroups = 10;
        long delay = 2;
        String serverURL = "http://ec2-35-87-143-25.us-west-2.compute.amazonaws.com:8080/Server_Web";
//        String serverURL = "http://ec2-35-87-143-25.us-west-2.compute.amazonaws.com:8080/go";
        int callsPerThread = 1000;
        int maxThreads = threadGroupSize * numThreadGroups;
        int totalCalls = numThreadGroups * threadGroupSize * callsPerThread * 2;

        // Initialize Executor service
        ExecutorService service = Executors.newFixedThreadPool(maxThreads);
        totalThreadsLatch = new CountDownLatch(threadGroupSize);

        // Initialize threads
        long start = System.currentTimeMillis();
        for (int i = 0; i < INITIAL_THREAD_COUNT; i++) {
            service.execute(new AlbumThreadRunnable(INITIAL_CALLS_PER_THREAD, serverURL));
        }

        totalThreadsLatch.await();
        long end = System.currentTimeMillis();
        System.out.println("Wall time: " + (end - start) * .001 + " s");


//        CountDownLatch tempLatch = new CountDownLatch(1);
//        printResults("Loading Initialization", INITIAL_THREAD_COUNT * INITIAL_CALLS_PER_THREAD * 2, INITIAL_THREAD_COUNT, end, start, tempLatch);
//        tempLatch.await();

        // Redefining variables for loading the server
        totalThreadsLatch = new CountDownLatch(maxThreads);
//        SUCCESSFUL_REQ.set(0);
//        FAILED_REQ.set(0);
//        TIME_EACH_REQUEST.set(0);

        start = System.currentTimeMillis();
        for (int i = 0; i < numThreadGroups; i++) {
            for (int j = 0; j < threadGroupSize; j++) {
                service.execute(new AlbumThreadRunnable(callsPerThread, serverURL));
            }

            // Sleep for delay amount of time, converted to seconds
            sleep(delay * 1000);
        }

//      Shutdown the executor and wait for all tasks to complete
        totalThreadsLatch.await();
        service.shutdown();
        end = System.currentTimeMillis();
        System.out.println("Wall time: " + (end - start) * .001 + " s");

//        printResults("Loading Server", totalCalls, maxThreads, end, start, new CountDownLatch(0));
    }

    /**
     * Simple method to print the results of the initialization phase and loading phase to the CL.
     *
     * @param totalCalls - The total requests made to the api.
     * @param maxThreads - The maximum amount of threads that could be running at once.
     * @param end        - The end time of the current phase.
     * @param start      - The start time of the current phase.
     */
    protected static void printResults(String currentPhase, int totalCalls, int maxThreads, long end, long start, CountDownLatch tempLatch) throws InterruptedException {
//        System.out.println("Successful req: " + SUCCESSFUL_REQ);
//        System.out.println("Failed req: " + FAILED_REQ);
//        long avgTimeRequest = (TIME_EACH_REQUEST.get() / totalCalls);

//        System.out.println("Avg time each request: " + avgTimeRequest + "ms");
//        System.out.println("Throughput: " + maxThreads / (avgTimeRequest * 0.001) + " threads per second");
        System.out.println("Wall time: " + (end - start) * .001 + " s");

//        new WriteToCsv(currentPhase, maxThreads, totalCalls, SUCCESSFUL_REQ.get(), FAILED_REQ.get(), avgTimeRequest, maxThreads / (avgTimeRequest * 0.001), (end - start) * .001).writeTestResults();
        tempLatch.countDown();
    }
}