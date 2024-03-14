package edu.ucdavis.dss.ipa.utilities;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Service
public class S3Service {
    private final S3Client s3;
    @Value("${AWS_S3_BUCKET}")
    String bucketName;

    public S3Service(S3Client s3) {
        this.s3 = s3;
    }

    public void upload(String filename, byte[] bytes) {
        PutObjectRequest objectRequest = PutObjectRequest.builder()
            .bucket(bucketName)
            .key(filename)
            .contentType("application/vnd.openxmlformats-officedocumsent.spreadsheetml.sheet")
            .contentLength((long) bytes.length)
            .build();

        s3.putObject(objectRequest, RequestBody.fromBytes(bytes));
    }

    public byte[] download(String filename) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
            .bucket(bucketName)
            .key(filename)
            .build();

        try {
            return s3.getObjectAsBytes(getObjectRequest).asByteArray();
        } catch (S3Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public HeadObjectResponse getMetadata(String filename) {
        try {
            HeadObjectRequest headObjectRequest = HeadObjectRequest.builder().bucket(bucketName).key(filename).build();

            return s3.headObject(headObjectRequest);
        } catch (S3Exception e) {
            // File not found
            return null;
        }
    }

    @Async
    public void delete(String filename) {
        DeleteObjectRequest deleteObjectRequest =
            DeleteObjectRequest.builder().bucket(bucketName).key(filename).build();

        s3.deleteObject(deleteObjectRequest);
    }
}
