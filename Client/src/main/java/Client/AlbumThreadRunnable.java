package Client;

import io.swagger.client.ApiException;
import io.swagger.client.ApiResponse;
import io.swagger.client.api.DefaultApi;
import io.swagger.client.model.AlbumInfo;
import io.swagger.client.model.AlbumsProfile;
import io.swagger.client.model.ImageMetaData;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AlbumThreadRunnable implements Runnable {
    private final int numReqs;
    private final DefaultApi albumsApi;
    private final boolean initializationPhase;
    private int successfulReq;
    private int failedReq;
    private final List<Long> latenciesPost;
    private final List<Long> latenciesGet;
    private int counter;


    /**
     * Class constructor used to create a thread runnable.
     *
     * @param numReqs   - The number of each request type the thread should send (GET vs. POST).
     * @param serverUrl - The server url each request should target.
     */
    public AlbumThreadRunnable(int numReqs, String serverUrl, boolean initializationPhase) {
        this.numReqs = numReqs;
        this.albumsApi = new DefaultApi();
        this.albumsApi.getApiClient().setBasePath(serverUrl);
        this.initializationPhase = initializationPhase;
        this.successfulReq = 0;
        this.failedReq = 0;
        this.latenciesPost = new ArrayList<>();
        this.latenciesGet = new ArrayList<>();
        this.counter = 1999;
    }

    @Override
    public void run() {
        long start, end, currentLatency;
        ApiResponse<?> responseCode;

        // Perform 1000 POST & GET requests
        for (int k = 0; k < this.numReqs; k++) {

            // Make POST request
            start = System.currentTimeMillis();
            responseCode = makeApiRequest("POST", null);
            end = System.currentTimeMillis();
            currentLatency = end - start;

            this.latenciesPost.add(currentLatency);

            // Make GET request
            start = System.currentTimeMillis();
            makeApiRequest("GET", getPostUUID(responseCode));
            end = System.currentTimeMillis();
            currentLatency = end - start;

            this.latenciesGet.add(currentLatency);
        }

        // Decrement count down latch
        AlbumClient.totalThreadsLatch.countDown();

        // If initialization phase, do not update variables
        if (initializationPhase) {
            return;
        }

        // Bulk update variables that are tracked during loading phase
        AlbumClient.SUCCESSFUL_REQ.addAndGet(this.successfulReq);
        AlbumClient.FAILED_REQ.addAndGet(this.failedReq);
        AlbumClient.latenciesPost.addAll(this.latenciesPost);
        AlbumClient.latenciesGet.addAll(this.latenciesGet);
    }

    /**
     * Method that makes a request to the given Api instance. If the request fails, will re-try up to MAX_ATTEMPT times.
     *
     * @param requestMethod - The type of request to make (POST vs. GET).
     * @return - The response code of the request.
     */
    private ApiResponse<?> makeApiRequest(String requestMethod, String requestParameters) {
        ApiResponse<?> response = null;
        int attempts = 0;
        boolean isGetReq = requestMethod.equals("GET");

        int maxRetries = 5;
        while (attempts < maxRetries) {
            try {
                response = isGetReq ? getAlbum(requestParameters) : postAlbum();
                if (response.getStatusCode() == 200) {
                    this.successfulReq += 1;
                    return response;
                }
                attempts++;
            } catch (ApiException e) {
                attempts++;
            }
        }

        this.failedReq += 1;

        // The response code is expected to never be null. Can only be null if all attempts throw exception.
        assert response != null;
        return response;
    }

    /**
     * Method to make a GET Album request to the given api instance.
     *
     * @return - The response of the api call.
     * @throws ApiException - If fails to call the API, e.g. server error or cannot deserialize the response body.
     */
    private ApiResponse<AlbumInfo> getAlbum(String albumID) throws ApiException {
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
        AlbumsProfile profile = new AlbumsProfile().artist("Monkey D. Luffy").title("One Piece").year(String.valueOf(this.counter++));
        return this.albumsApi.newAlbumWithHttpInfo(image, profile);
    }

    /**
     * Method to get the UUID from the POST response.
     *
     * @param response - The POST response.
     * @return - The UUID to use in the GET request.
     */
    private String getPostUUID(ApiResponse<?> response) {
        ImageMetaData imageMetaData = (ImageMetaData) response.getData();
        return imageMetaData.getAlbumID();
    }
}
