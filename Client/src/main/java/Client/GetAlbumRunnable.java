package Client;

import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.ApiResponse;
import io.swagger.client.api.LikeApi;

import java.util.ArrayList;
import java.util.List;

public class GetAlbumRunnable implements Runnable {
    private final LikeApi likeApi;
    private int successfulReq;
    private int failedReq;
    private final List<Long> reviewGet;


    /**
     * Class constructor used to create a thread runnable.
     *
     * @param serverUrl - The server url each request should target.
     */
    public GetAlbumRunnable(String serverUrl) {
        ApiClient apiClient = new ApiClient();
        apiClient.setBasePath(serverUrl);
        this.likeApi = new LikeApi(apiClient);

        this.successfulReq = 0;
        this.failedReq = 0;
        this.reviewGet = new ArrayList<>();
    }

    @Override
    public void run() {
        long start, end, currentLatency;
        while (AlbumClient.MAKE_GET_REQS) {
            try {
                String albumId = AlbumClient.albumIdsToCall.pollFirst();
                start = System.currentTimeMillis();
                ApiResponse<?> response = getAlbumReview(albumId);
                end = System.currentTimeMillis();

                int statusCode = response.getStatusCode();
                if (statusCode == 200) {
                    this.successfulReq += 1;
                } else {
                    this.failedReq += 1;
                }

                currentLatency = end - start;
                this.reviewGet.add(currentLatency);

                AlbumClient.albumIdsToCall.add(albumId);
            } catch (Exception ignored) {
            }
        }

        AlbumClient.getReqThreadsLatch.countDown();

        // Bulk update variables that are tracked during loading phase
        AlbumClient.SUCCESSFUL_GET_REQ.addAndGet(this.successfulReq);
        AlbumClient.FAILED_GET_REQ.addAndGet(this.failedReq);
        AlbumClient.reviewGet.addAll(this.reviewGet);
    }

    /**
     * Method that makes a request to the given Api instance. If the request fails, will re-try up to MAX_ATTEMPT times.
     */
    private void makeApiRequest(String albumId) {
        ApiResponse<?> response = null;
        int attempts = 0;

        int maxRetries = 5;
        while (attempts < maxRetries) {
            try {
                response = getAlbumReview(albumId);
                int statusCode = response.getStatusCode();
                if (statusCode == 200) {
                    this.successfulReq += 1;
                    return;
                }
                sleepThread(attempts++);
            } catch (ApiException e) {
                sleepThread(attempts++);
            }
        }

        this.failedReq += 1;

        // The response code is expected to never be null. Can only be null if all attempts throw exception.
        assert response != null;
    }

    /**
     * Method to make a GET Review request.
     *
     * @param albumId - The album ID to get the review data for.
     * @return - The response of the api call.
     * @throws ApiException - If fails to call the API, e.g. server error or cannot deserialize the response body.
     */
    private ApiResponse<?> getAlbumReview(String albumId) throws ApiException {
        return this.likeApi.getLikesWithHttpInfo(albumId);
    }

    /**
     * Method to prevent cascading failures. Introduced exponential backoff.
     *
     * @param numTries - The current attempt used to defined how long the thread should sleep.
     */
    private void sleepThread(int numTries) {
        try {
            Thread.sleep(2 ^ numTries);
        } catch (InterruptedException e){
            e.printStackTrace();
        }
    }
}
