package Part1;

import io.swagger.client.ApiException;
import io.swagger.client.ApiResponse;
import io.swagger.client.api.DefaultApi;
import io.swagger.client.model.AlbumInfo;
import io.swagger.client.model.AlbumsProfile;
import io.swagger.client.model.ImageMetaData;

import java.io.File;

public class AlbumThreadRunnable implements Runnable {
    private final int numReqs;
    private final String serverUrl;
    private int successfulReq;
    private int failedReq;
    private long timeEachReq;


    public AlbumThreadRunnable(int numReqs, String serverUrl) {
        this.numReqs = numReqs;
        this.serverUrl = serverUrl;
        this.successfulReq = 0;
        this.failedReq = 0;
        this.timeEachReq = 0;
    }

    @Override
    public void run() {
        DefaultApi albumsApi = new DefaultApi();
        albumsApi.getApiClient().setBasePath(serverUrl);

        long start;
        long end;
        long timeEachReq = 0;
        // Perform 1000 POST requests
        for (int k = 0; k < this.numReqs; k++) {
            start = System.currentTimeMillis();
            makeApiRequest("POST", albumsApi);
            end = System.currentTimeMillis();
            this.timeEachReq += (end - start);
        }

        // Perform 1000 GET requests
        for (int k = 0; k < this.numReqs; k++) {
            start = System.currentTimeMillis();
            makeApiRequest("GET", albumsApi);
            end = System.currentTimeMillis();
            this.timeEachReq += (end - start);
        }

        // Bulk update variables that are tracked
        AlbumClient.SUCCESSFUL_REQ.addAndGet(this.successfulReq);
        AlbumClient.FAILED_REQ.addAndGet(this.failedReq);
        AlbumClient.TIME_EACH_REQUEST.addAndGet(this.timeEachReq);
        AlbumClient.totalThreadsLatch.countDown();
    }


    /**
     * Method that makes a request to the given Api instance.
     *
     * @param albumRequest - The type of request to make.
     * @param albumsApi    - The api instance for the current thread.
     */
    private void makeApiRequest(String albumRequest, DefaultApi albumsApi) {
        ApiResponse<?> response;
        int attempts = 0;
        boolean isGetReq = albumRequest.equals("GET");

        try {
            while (attempts != 5) {
                response = isGetReq ? getAlbum(albumsApi) : postAlbum(albumsApi);

                if (response.getStatusCode() == 200) {
                    this.successfulReq += 1;
                    return;
                }
                attempts++;
            }
            this.failedReq += 1;
        } catch (ApiException e) {
            System.err.printf("Exception when calling DefaultApi %s%n", albumRequest);
            e.printStackTrace();
        }
    }

    /**
     * Method to make a GET Album request to the given api instance.
     *
     * @param albumsApi - The api instance to make the GET request to.
     * @return - The response of the api call.
     * @throws ApiException - If fails to call the API, e.g. server error or cannot deserialize the response body.
     */
    private ApiResponse<AlbumInfo> getAlbum(DefaultApi albumsApi) throws ApiException {
        String albumID = "3"; // String | path  parameter is album key to retrieve
        return albumsApi.getAlbumByKeyWithHttpInfo(albumID);
    }

    /**
     * Method to make a POST Album request to the given api instance.
     *
     * @param albumsApi - The api instance to make the POST request to.
     * @return - The response of the api call.
     * @throws ApiException - If fails to call the API, e.g. server error or cannot deserialize the response body.
     */
    private ApiResponse<ImageMetaData> postAlbum(DefaultApi albumsApi) throws ApiException {
        File image = new File("C:\\Users\\Peter\\Northeastern\\CS6650\\CS6650-Assignment1\\Client\\testingImage.png");
        AlbumsProfile profile = new AlbumsProfile();
        return albumsApi.newAlbumWithHttpInfo(image, profile);
    }
}
