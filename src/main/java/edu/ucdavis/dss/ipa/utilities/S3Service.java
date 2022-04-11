package edu.ucdavis.dss.ipa.utilities;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class S3Service {
    private final AmazonS3 s3client;
    @Value("${AWS_S3_BUCKET}")
    String s3Bucket;

    public S3Service(AmazonS3 s3client) {
        this.s3client = s3client;
    }

    public void upload(String filename, byte[] bytes) {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType("application/vnd.openxmlformats-officedocumsent.spreadsheetml.sheet");
        metadata.setContentLength(Long.valueOf(bytes.length));

        ByteArrayInputStream is = new ByteArrayInputStream(bytes);

        s3client.putObject(s3Bucket, filename, is, metadata);
    }

    public byte[] download(String filename) {
        S3Object object = s3client.getObject(s3Bucket, filename);
        try {
            return IOUtils.toByteArray(object.getObjectContent());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public ObjectMetadata getMetadata(String filename) {
        try {
            ObjectMetadata omd = s3client.getObjectMetadata(s3Bucket, filename);
            return omd;
        } catch (AmazonS3Exception e) {
            // File not found
            return null;
        }
    }

    @Async
    public void delete(String filename) {
        s3client.deleteObject(s3Bucket, filename);
    }
}
