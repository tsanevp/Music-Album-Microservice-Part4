package Part1;

import Part1.Model.Album;
import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.ApiResponse;
import io.swagger.client.api.DefaultApi;
import io.swagger.client.model.AlbumInfo;
import io.swagger.client.model.AlbumsBody;
import io.swagger.client.model.AlbumsProfile;
import io.swagger.client.model.ImageMetaData;

import javax.sound.midi.SysexMessage;
import java.io.File;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static java.lang.Thread.sleep;

public class AlbumClient {

    protected static final AtomicInteger SUCCESSFUL_REQ = new AtomicInteger(0);
    protected static final AtomicInteger FAILED_REQ = new AtomicInteger(0);
    protected static final AtomicLong TIME_EACH_REQUEST = new AtomicLong(0);
    protected static CountDownLatch totalThreadsLatch;

    private final int INITIAL_THREAD_COUNT = 10;
    private final int INITIAL_CALLS_PER_THREAD = 100;
    public AlbumClient() {}

    public static void main(String[] args) throws InterruptedException, ApiException {
//        int callsPerThread = 1000;
//
//        int threadGroupSize = Integer.parseInt(args[0]);
//        int numThreadGroups = Integer.parseInt(args[1]);
//        long delay = Integer.parseInt(args[2]);
//        String serverURL = String.valueOf(Integer.parseInt(args[3]));

        // Define initial arguments
        int threadGroupSize = 10;
        int callsPerThread = 100;
        int numThreadGroups = 10;
        long delay = 2;
        String serverURL = "http://ec2-54-245-221-2.us-west-2.compute.amazonaws.com:8080/Server_Web/";
        ApiClient apiClient = new ApiClient().setBasePath(serverURL);
        AlbumClient albumClient = new AlbumClient();

//        long timeTaken = 0;
//        long start;
//        long end;
//        DefaultApi api = new DefaultApi(apiClient);
//        long startLoop = System.currentTimeMillis();
//        for (int i = 0; i < 1000; i++) {
//            start = System.currentTimeMillis();
//            albumClient.getAlbum(api);
//            end = System.currentTimeMillis();
//            atomicLong.addAndGet(end - start);
//
//            start = System.currentTimeMillis();
//            albumClient.postAlbum(api);
//            end = System.currentTimeMillis();
//            atomicLong.addAndGet(end - start);
//        }
//        end = System.currentTimeMillis();
//        System.out.println("Total time: " + (end - startLoop) * .001);
//
//        System.out.println(atomicLong.get() / 2000);

//        // Totals calls made to post and get
        int totalCalls = numThreadGroups * threadGroupSize * callsPerThread * 2;

        totalThreadsLatch = new CountDownLatch(threadGroupSize * numThreadGroups);
        ExecutorService service = Executors.newFixedThreadPool(threadGroupSize);
//        BlockingQueue<String> dataBuffer = new LinkedBlockingQueue<>();


        // Run initial 10 threads on startup
        long start = System.currentTimeMillis();
        albumClient.initialThreads(apiClient, service);
        long end = System.currentTimeMillis();
        System.out.println("initialization:" + (end - start));

        start = System.currentTimeMillis();
//        executeThreadGroups(numThreadGroups, threadGroupSize, delay, serverURL, callsPerThread, service);
        for (int i = 0; i < numThreadGroups; i++) {
            for (int j = 0; j < threadGroupSize; j++) {
                service.execute(new AlbumThreadRunnable(callsPerThread, serverURL));
            }
            // Sleep for delay amount of time, converted to seconds
            sleep(delay * 1000);
        }

//         Shutdown the executor and wait for all tasks to complete
        totalThreadsLatch.await();
        service.shutdown();
        end = System.currentTimeMillis();

        System.out.println("Successful req: " + SUCCESSFUL_REQ);
        System.out.println("Failed req: " + FAILED_REQ);
        long avgTimeRequest = (TIME_EACH_REQUEST.get() / totalCalls);

        System.out.println("Avg time each request: " + avgTimeRequest + "ms");
        System.out.println("Throughput: " + threadGroupSize / (avgTimeRequest * 0.001) + " threads per second");
        System.out.println("Wall time: " + (end - start) * .001 + " s");

    }

//    /**
//     * Method that runs creates each thread group and calls methods to execute the treads in each group.
//     * @param numThreadGroups - The number of thread groups to create and execute.
//     * @param threadGroupSize - The number of threads in each thread group.
//     * @param callsPerThread - The number of requests each thread should make.
//     * @param service - An ExecutorService thread pool with the number of threads in each group.
//     * @param delay - The amount of time to delay between thread groups.
//     * @throws InterruptedException - Thrown if thread is interrupted while busy.
//     */
//    private static void executeThreadGroups(int numThreadGroups, int threadGroupSize, long delay, String serverUrl, int callsPerThread, ExecutorService service) throws InterruptedException {
//        for (int i = 0; i < numThreadGroups; i++) {
//            for (int j = 0; j < threadGroupSize; j++) {
//                service.execute(new AlbumThreadRunnable(callsPerThread, serverUrl));
//            }
//            // Sleep for delay amount of time, converted to seconds
//            sleep(delay * 1000);
//        }
//    }

//    /**
//     * Method to execute a single thread group, creating threadGroupSize amount of threads that each call a number of
//     * requests.
//     *
//     * @param threadGroupSize - - The number of threads in the thread group.
//     * @param service - An ExecutorService thread pool with the number of threads in each group.
////     * @param apiClient - The api client instance used to create an HTTP client for later requests.
//     * @param callsPerThread - The number of requests each thread should make.
//     * @param successfulPost - An atomic integer tracking the number of successful POST requests.
//     * @param successfulGet - An atomic integer tracking the number of successful GET requests.
//     */
//    private void executeThreadGroup(int threadGroupSize, ExecutorService service, String serverUrl, int callsPerThread, AtomicInteger successfulPost, AtomicInteger successfulGet, AtomicLong atomicLong) {
//        for (int j = 0; j < threadGroupSize; j++) {
//            service.execute(new AlbumThreadRunnable(callsPerThread, serverUrl));
//        }
//    }

    /**
     * Method to create the initial threads prior to loading the server.
     * @param apiClient - The api client instance used to create an HTTP client for later requests.
     * @param service - An ExecutorService thread pool with the number of threads in each group.
     * @throws InterruptedException - Thrown if thread is interrupted while busy.
     */
    private void initialThreads(ApiClient apiClient, ExecutorService service) throws InterruptedException {
        CountDownLatch initialLatch = new CountDownLatch(INITIAL_THREAD_COUNT * INITIAL_CALLS_PER_THREAD * 2);
        AtomicInteger successfulPost = new AtomicInteger(0);
        AtomicInteger successfulGet = new AtomicInteger(0);

        for (int i = 0; i < INITIAL_THREAD_COUNT; i++) {
            service.execute(() -> {
                try {
                    DefaultApi defaultApi = new DefaultApi(apiClient);

                    // Perform 100 POST requests
                    for (int k = 0; k < INITIAL_CALLS_PER_THREAD; k++) {
                        makeApiRequest("POST", defaultApi, successfulPost);
                        initialLatch.countDown();
                    }

                    // Perform 100 GET requests
                    for (int k = 0; k < INITIAL_CALLS_PER_THREAD; k++) {
                        makeApiRequest("GET", defaultApi, successfulGet);
                        initialLatch.countDown();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

        initialLatch.await();
        System.out.println("Successful initial Gets: " + successfulGet);
        System.out.println("Successful initial Posts: " + successfulPost);
    }

    /**
     * Method that makes a request to the given Api instance.
     * @param albumRequest - The type of request to make.
     * @param apiInstance - The api instance for the current thread.
//     * @param callsPerGroupLatch - A countdown latch tracking the number of calls the thread  has group left to make.
     * @param successfulRequest - An atomic number tracking the number of successful calls made.
     */
    private void makeApiRequest(String albumRequest, DefaultApi apiInstance, AtomicInteger successfulRequest) {
        ApiResponse<?> response;
        int attempts = 0;
        boolean isGetReq = albumRequest.equals("GET");

        try {
            while (attempts != 5) {
                response = isGetReq ? getAlbum(apiInstance) : postAlbum(apiInstance);

                if (response.getStatusCode() == 200) {
                    successfulRequest.incrementAndGet();
                    break;
                }

                attempts++;
            }
        } catch (ApiException e) {
            System.err.printf("Exception when calling DefaultApi %s%n", albumRequest);
            e.printStackTrace();
        }
    }

    /**
     * Method to make a GET Album request to the given api instance.
     * @param apiInstance - The api instance to make the GET request to.
     * @return - The response of the api call.
     * @throws ApiException - If fails to call the API, e.g. server error or cannot deserialize the response body.
     */
    private ApiResponse<AlbumInfo> getAlbum(DefaultApi apiInstance) throws ApiException {
        String albumID = "3"; // String | path  parameter is album key to retrieve
        return apiInstance.getAlbumByKeyWithHttpInfo(albumID);
    }

    /**
     * Method to make a POST Album request to the given api instance.
     * @param apiInstance - The api instance to make the POST request to.
     * @return - The response of the api call.
     * @throws ApiException - If fails to call the API, e.g. server error or cannot deserialize the response body.
     */
    private ApiResponse<ImageMetaData> postAlbum(DefaultApi apiInstance) throws ApiException {
        File image = new File("C:\\Users\\Peter\\Northeastern\\CS6650\\CS6650-Assignment1\\Client\\testingImage.png");
        AlbumsProfile profile = new AlbumsProfile();
        return apiInstance.newAlbumWithHttpInfo(image, profile);
    }
}