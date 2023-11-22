package Client;

import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.ApiResponse;
import io.swagger.client.api.DefaultApi;
import io.swagger.client.api.LikeApi;
import io.swagger.client.model.AlbumsProfile;
import io.swagger.client.model.ImageMetaData;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AlbumThreadRunnable implements Runnable {
    private final int numReqs;
    private final DefaultApi albumsApi;
    private final LikeApi likeApi;
    private final boolean initializationPhase;
    private int successfulReq;
    private int failedReq;
    private final List<Long> albumPost;
    private final List<Long> likePost;
    private final List<Long> dislikePost;
    private int counter;


    /**
     * Class constructor used to create a thread runnable.
     * http://ec2-34-219-17-241.us-west-2.compute.amazonaws.com:8080/Server_Web
     * @param numReqs   - The number of each request type the thread should send (GET vs. POST).
     * @param serverUrl - The server url each request should target.
     */
    public AlbumThreadRunnable(int numReqs, String serverUrl, boolean initializationPhase) {
        this.numReqs = numReqs;

        ApiClient apiClient = new ApiClient();
        apiClient.setBasePath(serverUrl);
        this.albumsApi = new DefaultApi(apiClient);
        this.likeApi = new LikeApi(apiClient);

        this.initializationPhase = initializationPhase;
        this.successfulReq = 0;
        this.failedReq = 0;
        this.albumPost = new ArrayList<>();
        this.likePost = new ArrayList<>();
        this.dislikePost = new ArrayList<>();

        this.counter = 1999;
    }

    @Override
    public void run() {
        long start, end, currentLatency;
        ApiResponse<?> response;
        String uuid = null;
        String[] requestParameters;


        // Perform 1000 POST & GET requests
        for (int k = 0; k < this.numReqs; k++) {
//          // For loop to send 4 POST requests (1 album POST, 3 review POSTs - 2 likes, 1 dislike)
            for (int i = 0; i < 4; i++) {
                requestParameters = switch (i) {
                    case 0 -> new String[]{"albumPost", null};
                    case 1, 2 -> new String[]{"like", uuid};
                    case 3 -> new String[]{"dislike", uuid};
                    default -> new String[]{};
                };

                start = System.currentTimeMillis();
                response = makeApiRequest(requestParameters);
                end = System.currentTimeMillis();
                currentLatency = end - start;

                switch (i) {
                    case 0 -> {
                        uuid = getPostUUID(response);
                        this.albumPost.add(currentLatency);
                    }
                    case 3 -> this.dislikePost.add(currentLatency);
                    default -> this.likePost.add(currentLatency);
                }
            }
        }

        // Decrement count down latch
        AlbumClient.totalThreadsLatch.countDown();

        // TODO: uncomment this out when actually testing
        // If initialization phase, do not update variables
//        if (initializationPhase) return;

        // Bulk update variables that are tracked during loading phase
        AlbumClient.SUCCESSFUL_REQ.addAndGet(this.successfulReq);
        AlbumClient.FAILED_REQ.addAndGet(this.failedReq);
        AlbumClient.albumPost.addAll(this.albumPost);
        AlbumClient.likesPost.addAll(this.likePost);
        AlbumClient.dislikesPost.addAll(this.dislikePost);
    }

    /**
     * Method that makes a request to the given Api instance. If the request fails, will re-try up to MAX_ATTEMPT times.
     *
     * @return - The response code of the request.
     */
    private ApiResponse<?> makeApiRequest(String[] requestParameters) {
        ApiResponse<?> response = null;
        int attempts = 0;

        int maxRetries = 5;
        while (attempts < maxRetries) {
            try {
                response = Objects.equals(requestParameters[0], "albumPost") ? postAlbum() : sendReview(requestParameters);
                int statusCode = response.getStatusCode();
                if (statusCode == 200 || statusCode == 201) {
                    this.successfulReq += 1;
                    return response;
                }
                attempts++;
            } catch (ApiException e) {
                sleepThread(attempts++);
            }
        }

        this.failedReq += 1;

        // The response code is expected to never be null. Can only be null if all attempts throw exception.
        assert response != null;
        return response;
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
     * Method to make a POST Review request.
     *
     * @param requestParameters - The POST request parameters. The review type and uuid.
     * @return - The response of the api call.
     * @throws ApiException - If fails to call the API, e.g. server error or cannot deserialize the response body.
     */
    private ApiResponse<?> sendReview(String[] requestParameters) throws ApiException {
        String reviewType = requestParameters[0];
        String uuid = requestParameters[1];

        return this.likeApi.reviewWithHttpInfo(reviewType, uuid);
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

    private void sleepThread(int numTries) {
        try {
            Thread.sleep(2 ^ numTries);
        } catch (InterruptedException e){
            e.printStackTrace();
        }
    }
}
