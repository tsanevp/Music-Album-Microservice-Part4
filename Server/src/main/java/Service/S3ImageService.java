package Service;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import org.apache.commons.io.IOUtils;

import javax.servlet.http.Part;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static Util.Constants.BUCKET_NAME;

public class S3ImageService {
    private final AmazonS3 s3Client;
    private final TransferManager transferManager;

    public S3ImageService() {
        this.s3Client = AmazonS3ClientBuilder.standard()
                .withRegion(Regions.US_WEST_2)
                .build();
        this.transferManager = TransferManagerBuilder.standard()
                .withS3Client(s3Client)
                .build();
    }

    public void uploadImage(Part imagePart, String imageKey) {
        InputStream inputStream;
        try {
            inputStream = imagePart.getInputStream();

            byte[] contents = IOUtils.toByteArray(inputStream);
            InputStream stream = new ByteArrayInputStream(contents);

            ObjectMetadata meta = new ObjectMetadata();
            meta.setContentLength(contents.length);
            meta.setContentType("image/png");

            transferManager.upload(BUCKET_NAME, imageKey, stream, meta);

            stream.close();
            inputStream.close();
        } catch (IOException ignored) {
        }
    }

    public void shutDown() {
        transferManager.shutdownNow();
        s3Client.shutdown();
    }
}