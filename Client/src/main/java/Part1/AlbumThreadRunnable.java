package Part1;

import io.swagger.client.ApiException;
import io.swagger.client.ApiResponse;
import io.swagger.client.api.DefaultApi;
import io.swagger.client.model.AlbumInfo;
import io.swagger.client.model.AlbumsProfile;
import io.swagger.client.model.ImageMetaData;

import java.io.File;

public class AlbumThreadRunnable implements Runnable {
    private final int MAX_ATTEMPTS = 5;

    private final int numReqs;
    private long sumReqLatencies;
    private int successfulReq;
    private int failedReq;
    private final DefaultApi albumsApi;


    public AlbumThreadRunnable(int numReqs, String serverUrl) {
        this.numReqs = numReqs;
        this.albumsApi = new DefaultApi();
        this.albumsApi.getApiClient().setBasePath(serverUrl);
        this.successfulReq = 0;
        this.failedReq = 0;
        this.sumReqLatencies = 0;
    }

    @Override
    public void run() {
        long start;
        long end;

        // Perform 1000 POST requests
        for (int k = 0; k < this.numReqs; k++) {
            start = System.currentTimeMillis();
            makeApiRequest("POST");
            end = System.currentTimeMillis();
            this.sumReqLatencies += (end - start);
        }

        // Perform 1000 GET requests
        for (int k = 0; k < this.numReqs; k++) {
            start = System.currentTimeMillis();
            makeApiRequest("GET");
            end = System.currentTimeMillis();
            this.sumReqLatencies += (end - start);
        }

        // Bulk update variables that are tracked
        AlbumClient.SUCCESSFUL_REQ.addAndGet(this.successfulReq);
        AlbumClient.FAILED_REQ.addAndGet(this.failedReq);
        AlbumClient.SUM_LATENCY_EACH_REQ.addAndGet(this.sumReqLatencies);
        AlbumClient.totalThreadsLatch.countDown();
    }

    /**
     * Method that makes a request to the given Api instance. If the request fails, will re-try up to MAX_ATTEMPT times.
     *
     * @param requestMethod - The type of request to make (POST vs. GET).
     */
    private void makeApiRequest(String requestMethod) {
        ApiResponse<?> response;
        int attempts = 0;
        boolean isGetReq = requestMethod.equals("GET");

        while (attempts < MAX_ATTEMPTS) {
            try {
                response = isGetReq ? getAlbum() : postAlbum();

                if (response.getStatusCode() == 200) {
                    this.successfulReq += 1;
                    return;
                }
                attempts++;
            } catch (ApiException e) {
                attempts++;
            }
        }

        this.failedReq += 1;
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
