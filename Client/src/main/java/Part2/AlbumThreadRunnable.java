package Part2;

import io.swagger.client.ApiException;
import io.swagger.client.ApiResponse;
import io.swagger.client.api.DefaultApi;
import io.swagger.client.model.AlbumInfo;
import io.swagger.client.model.AlbumsProfile;
import io.swagger.client.model.ImageMetaData;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static Part2.AlbumClient.writeToCsv;

public class  AlbumThreadRunnable implements Runnable {
    private final int numReqs;
    private long sumReqLatencies;
    private int successfulReq;
    private int failedReq;
    private final DefaultApi albumsApi;
    private final ArrayList<String[]> threadResults;
    private final List<Long> latencies;


    /**
     * Class constructor used to create a thread runnable.
     *
     * @param numReqs - The number of each request type the thread should send (GET vs. POST).
     * @param serverUrl - The server url each request should target.
     */
    public AlbumThreadRunnable(int numReqs, String serverUrl) {
        this.numReqs = numReqs;
        this.albumsApi = new DefaultApi();
        this.albumsApi.getApiClient().setBasePath(serverUrl);
        this.successfulReq = 0;
        this.failedReq = 0;
        this.sumReqLatencies = 0;
        this.threadResults = new ArrayList<>();
        this.latencies = new ArrayList<>();
    }

    @Override
    public void run() {
        long start, end, currentLatency;
        int responseCode;
        int requestGroups = 5;

        for (int i = 0; i < requestGroups; i++) {
            // Perform 1000 POST requests
            for (int k = 0; k < this.numReqs / requestGroups; k++) {
                start = System.currentTimeMillis();
                responseCode = makeApiRequest("POST");
                end = System.currentTimeMillis();
                currentLatency = end - start;

                this.sumReqLatencies += currentLatency;
                this.latencies.add(currentLatency);
                threadResults.add(new String[]{String.valueOf(start), "POST", String.valueOf((end - start)), String.valueOf(responseCode)});
            }

            // Perform 1000 GET requests
            for (int k = 0; k < this.numReqs / requestGroups; k++) {
                start = System.currentTimeMillis();
                responseCode = makeApiRequest("GET");
                end = System.currentTimeMillis();
                currentLatency = end - start;

                this.sumReqLatencies += currentLatency;
                this.latencies.add(currentLatency);
                threadResults.add(new String[]{String.valueOf(start), "GET", String.valueOf((end - start)), String.valueOf(responseCode)});
            }
        }

        // Bulk update variables that are tracked
        AlbumClient.SUCCESSFUL_REQ.addAndGet(this.successfulReq);
        AlbumClient.FAILED_REQ.addAndGet(this.failedReq);
        AlbumClient.SUM_LATENCY_EACH_REQ.addAndGet(this.sumReqLatencies);
        AlbumClient.latencies.addAll(this.latencies);
        AlbumClient.totalThreadsLatch.countDown();

        writeToCsv.writeLoadTestResultsToSheet(threadResults);

//        try {
//            AlbumClient.resultsBuffer.put(threadResults);
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }

    }

    /**
     * Method that makes a request to the given Api instance. If the request fails, will re-try up to MAX_ATTEMPT times.
     *
     * @param requestMethod - The type of request to make (POST vs. GET).
     * @return - The response code of the request.
     */
    private int makeApiRequest(String requestMethod) {
        ApiResponse<?> response = null;
        int attempts = 0;
        boolean isGetReq = requestMethod.equals("GET");

        int maxRetries = 5;
        while (attempts < maxRetries) {
            try {
                response = isGetReq ? getAlbum() : postAlbum();

                if (response.getStatusCode() == 200) {
                    this.successfulReq += 1;
                    return response.getStatusCode();
                }
                attempts++;
            } catch (ApiException e) {
                attempts++;
            }
        }

        this.failedReq += 1;

        // The response code is expected to never be null. Can only be null if all attempts throw exception.
        assert response != null;
        return response.getStatusCode();
    }

    /**
     * Method to make a GET Album request to the given api instance.
     *
     * @return - The response of the api call.
     * @throws ApiException - If fails to call the API, e.g. server error or cannot deserialize the response body.
     */
    private ApiResponse<AlbumInfo> getAlbum() throws ApiException {
        String albumID = "3";
        return this.albumsApi.getAlbumByKeyWithHttpInfo(albumID);
    }

    /**
     * Method to make a POST Album request to the given api instance.
     *
     * @return - The response of the api call.
     * @throws ApiException - If fails to call the API, e.g. server error or cannot deserialize the response body.
     */
    private ApiResponse<ImageMetaData> postAlbum() throws ApiException {
        File image = new File("src/main/java/testingImage.png");
        AlbumsProfile profile = new AlbumsProfile().artist("Monkey D. Luffy").title("One Piece").year("1999");
        return this.albumsApi.newAlbumWithHttpInfo(image, profile);
    }
}
