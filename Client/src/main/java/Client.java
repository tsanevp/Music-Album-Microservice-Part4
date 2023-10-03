import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.api.DefaultApi;
import io.swagger.client.model.AlbumInfo;
import io.swagger.client.model.AlbumsProfile;
import io.swagger.client.model.ImageMetaData;

import java.io.File;

public class Client {
    public static void main(String[] args) throws InterruptedException {
//        AlbumClient defaultApiExample = new AlbumClient();

        // Example
        int threadGroupSize = 10;

        int numThreadGroups = 1;
        long delay = 100;
        String serverURL = "http://ec2-54-191-235-244.us-west-2.compute.amazonaws.com:8080/Server_Web/";
        ApiClient apiClient = new ApiClient();
        apiClient.setBasePath(serverURL);


        getAlbum(apiClient);
        // Run initial 10 threads on startup
//        initialThreads(threadGroupSize, numThreadGroups, delay, serverURL);

        long theadGroupExecutionStartTime = System.currentTimeMillis();
//        this.executeThreadGroups(threadGroupSize, numThreadGroups, delay, serverURL);

    }

    private static void getAlbum(ApiClient apiClient) {
        DefaultApi apiInstance = new DefaultApi(apiClient);

        String albumID = "3"; // String | path  parameter is album key to retrieve
        try {
            AlbumInfo result = apiInstance.getAlbumByKey(albumID);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling DefaultApi#getAlbumByKey");
            e.printStackTrace();
        }
    }

    private static void postAlbum(ApiClient apiClient) {
        DefaultApi apiInstance = new DefaultApi(apiClient);
        File image = new File("C:\\Users\\Peter\\Downloads\\photography.jpg"); // File |
        AlbumsProfile profile = new AlbumsProfile(); // AlbumsProfile |
        try {
            ImageMetaData result = apiInstance.newAlbum(image, profile);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling DefaultApi#newAlbum");
            e.printStackTrace();
        }
    }
}
