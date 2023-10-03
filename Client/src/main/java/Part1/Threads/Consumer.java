package Part1.Threads;

import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.api.DefaultApi;
import io.swagger.client.model.AlbumInfo;
import io.swagger.client.model.AlbumsProfile;
import io.swagger.client.model.ImageMetaData;

import java.io.File;
import java.util.concurrent.BlockingQueue;

public class Consumer implements Runnable {

    private BlockingQueue<String> buffer;

    public Consumer(BlockingQueue<String> buffer) {
        this.buffer = buffer;
    }

    @Override
    public void run() {
        String request;
//        for (int i = 0; i < numThreads)
    }

    private void getAlbum(ApiClient apiClient) {
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

    private void postAlbum(ApiClient apiClient) {
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
