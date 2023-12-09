package Client;

import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.ApiResponse;
import io.swagger.client.api.LikeApi;

import java.util.ArrayList;
import java.util.List;

public class GetAlbumRunnable implements Runnable {
    private final LikeApi likeApi;
    private final List<Long> reviewGet;
    private int successfulReq;
    private int failedReq;


    /**
     * Class constructor used to create a thread runnable.
     *
     * @param serverUrl - The server url each request should target.
     */
    public GetAlbumRunnable(String serverUrl) {
        ApiClient apiClient = new ApiClient();
        apiClient.setBasePath(serverUrl);
        apiClient.setReadTimeout(45);
        this.likeApi = new LikeApi(apiClient);

        this.successfulReq = 0;
        this.failedReq = 0;
        this.reviewGet = new ArrayList<>();
    }

    @Override
    public void run() {
        long start, end;
        while (AlbumClient.MAKE_GET_REQS) {
            if (AlbumClient.albumIdsToCall.isEmpty()) {
                continue;
            }

            String albumId = AlbumClient.albumIdsToCall.get(AlbumClient.randomInt.nextInt(AlbumClient.albumIdsToCall.size()));

            start = System.currentTimeMillis();
            getAlbumReview(albumId);
            end = System.currentTimeMillis();
            this.reviewGet.add(end - start);
        }

        AlbumClient.getReqThreadsLatch.countDown();

        // Bulk update variables that are tracked during loading phase
        AlbumClient.SUCCESSFUL_GET_REQ.addAndGet(this.successfulReq);
        AlbumClient.FAILED_GET_REQ.addAndGet(this.failedReq);
        AlbumClient.reviewGet.addAll(this.reviewGet);
    }

    /**
     * Method to make a GET Review request.
     *
     * @param albumId - The album ID to get the review data for.
     */
    private void getAlbumReview(String albumId) {
        try {
            ApiResponse<?> response = this.likeApi.getLikesWithHttpInfo(albumId);

            if (response.getStatusCode() != 200) {
                throw new Exception("Failed");
            }

            this.successfulReq += 1;

        } catch (Exception e) {
            this.failedReq += 1;
        }
    }
}
