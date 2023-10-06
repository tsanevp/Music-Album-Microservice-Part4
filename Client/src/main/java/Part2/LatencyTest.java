package Part2;

import io.swagger.client.ApiException;
import io.swagger.client.ApiResponse;
import io.swagger.client.api.DefaultApi;
import io.swagger.client.model.AlbumInfo;
import io.swagger.client.model.AlbumsProfile;
import io.swagger.client.model.ImageMetaData;

import java.io.File;
import java.util.concurrent.CountDownLatch;

public class LatencyTest {
    public static void main(String[] args) throws InterruptedException, ApiException {
        File image = new File("src/main/java/testingImage.png");
//        File image = new File("C:\\Users\\Peter\\Downloads\\SDE_Internship.docx");
        AlbumsProfile profile = new AlbumsProfile().artist("Oda").title("One Piece").year("1999");
        int numReqs = 100;

        String serverURL = "http://ec2-35-87-143-25.us-west-2.compute.amazonaws.com:8080/Server_Web";
//        String serverURL = "http://ec2-54-191-58-4.us-west-2.compute.amazonaws.com:8080/go";

        long start;
        long end;
        DefaultApi albumClient = new DefaultApi();
        albumClient.getApiClient().setBasePath(serverURL);

        ApiResponse<ImageMetaData> response = albumClient.newAlbumWithHttpInfo(image, profile);
//        ApiResponse<AlbumInfo> response = albumClient.getAlbumByKeyWithHttpInfo("3");

        System.out.println(response.getData());

        long latency = 0;
        long startLoop = System.currentTimeMillis();
////
        for (int i = 0; i < numReqs; i++) {
            start = System.currentTimeMillis();
            albumClient.newAlbumWithHttpInfo(image, profile);
            end = System.currentTimeMillis();
            latency += end - start;
        }
//
//        for (int i = 0; i < numReqs; i++) {
//            start = System.currentTimeMillis();
//            albumClient.getAlbumByKeyWithHttpInfo("3");
//            end = System.currentTimeMillis();
//            latency += end - start;
//        }
//
        end = System.currentTimeMillis();
        double wallTime = (end - startLoop) * .001;
        latency = latency / (numReqs * 2);
        double throughPut = 1 / (latency * 0.001);
        System.out.println("Total time: " + wallTime + "s");
        System.out.println("Avg time request: " + latency + "ms");
//
//        new WriteToCsv("LatencyTest", 1, numReqs * 2, 0, 0, latency, throughPut, wallTime).writeTestResults();
    }

}
