package Part2;

import io.swagger.client.ApiException;
import io.swagger.client.ApiResponse;
import io.swagger.client.api.DefaultApi;
import io.swagger.client.model.AlbumInfo;
import io.swagger.client.model.AlbumsProfile;
import io.swagger.client.model.ImageMetaData;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class AlbumThreadRunnable implements Runnable {
    private final int numReqs;
    private final String serverUrl;
//    private int successfulReq;
//    private int failedReq;
    private long timeEachReq;
    private ArrayList<String> groupResults;

    private final int MAX_ATTEMPTS = 5;

    private final File image = new File("src/main/java/testingImage.png");
    private final AlbumsProfile profile = new AlbumsProfile().artist("Luffy").title("One Piece").year("2023");



    public AlbumThreadRunnable(int numReqs, String serverUrl) {
        this.numReqs = numReqs;
        this.serverUrl = serverUrl;
//        this.successfulReq = 0;
//        this.failedReq = 0;
        this.timeEachReq = 0;
        this.groupResults = new ArrayList<>();
    }

    @Override
    public void run() {
//        System.out.println("Thread # " + Thread.currentThread().getId() + " now starting");
        DefaultApi albumsApi = new DefaultApi();
        albumsApi.getApiClient().setBasePath(serverUrl);

        long start;
        long end;

        // Perform 1000 POST requests
        for (int k = 0; k < this.numReqs; k++) {
            StringBuilder currentRequest = new StringBuilder();
            start = System.currentTimeMillis();
            currentRequest.append(start).append(",POST,");
            makeApiRequest("POST", albumsApi);
            end = System.currentTimeMillis();
            currentRequest.append(end - start).append(",");
//            System.out.println(end - start + " ms");
//            this.timeEachReq += (end - start);
            this.groupResults.add(currentRequest.toString());
        }

        // Perform 1000 GET requests
        for (int k = 0; k < this.numReqs; k++) {
            start = System.currentTimeMillis();

            makeApiRequest("GET", albumsApi);

            end = System.currentTimeMillis();
            this.timeEachReq += (end - start);
        }

        // Bulk update variables that are tracked
//        AlbumClient.SUCCESSFUL_REQ.addAndGet(this.successfulReq);
//        AlbumClient.FAILED_REQ.addAndGet(this.failedReq);
//        Part2.AlbumClient.TIME_EACH_REQUEST.addAndGet(this.timeEachReq);
//        try {
//            Part2.AlbumClient.resultsBuffer.put(groupResults);
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
        new Thread(new Producer(this.groupResults, Part2.AlbumClient.resultsBuffer));
        Part2.AlbumClient.totalThreadsLatch.countDown();
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

        while (attempts < MAX_ATTEMPTS) {
            try {
                response = isGetReq ? getAlbum(albumsApi) : postAlbum(albumsApi);

                if (response.getStatusCode() == 200) {
//                    this.successfulReq += 1;
                    return;
                }
                attempts++;
            } catch (ApiException e) {
//                e.printStackTrace();
                attempts++;
            }
//            threadSleep(attempts++);
        }
//        this.failedReq += 1;
    }

//    /**
//     * Method that makes a request to the given Api instance.
//     *
//     * @param albumRequest - The type of request to make.
//     * @param albumsApi    - The api instance for the current thread.
//     */
//    private void makeApiPostRequest(String albumRequest, DefaultApi albumsApi) {
//        ApiResponse<?> response;
//        int attempts = 0;
//
//        while (attempts < MAX_ATTEMPTS) {
//            try {
//                response = postAlbum(albumsApi);
////                postAlbum(albumsApi);
//                if (response.getStatusCode() == 200) {
//////                    this.successfulReq += 1;
//                    return;
//                }
////                attempts++;
//            } catch (ApiException e) {
////                e.printStackTrace();
//                attempts++;
//            }
//            attempts++;
////            threadSleep(attempts++);
//        }
//
////        this.failedReq += 1;
//    }
//    private void makeApiGetRequest(String albumRequest, DefaultApi albumsApi) {
//        ApiResponse<?> response;
//        int attempts = 0;
//
//        while (attempts < MAX_ATTEMPTS) {
//            try {
//                response = getAlbum(albumsApi);
//
//                if (response.getStatusCode() == 200) {
////                    this.successfulReq += 1;
//                    break;
//                }
//                attempts++;
//            } catch (ApiException e) {
////                e.printStackTrace();
//                attempts++;
//            }
//
////            threadSleep(attempts++);
//        }
//
////        this.failedReq += 1;
//    }

//    private void threadSleep(int attempts) {
//        try {
//            sleep(2 ^ attempts);
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
//    }

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
        return albumsApi.newAlbumWithHttpInfo(image, profile);
    }
}
