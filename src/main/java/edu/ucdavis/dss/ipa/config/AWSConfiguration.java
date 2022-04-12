package edu.ucdavis.dss.ipa.config;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AWSConfiguration {
    @Value("${AWS_ACCESS_KEY}")
    String awsAccessKey;

    @Value("${AWS_SECRET_KEY}")
    String awsSecretKey;

    @Value("${AWS_REGION}")
    String awsRegion;

    @Bean
    public AmazonS3 s3client() {
        AWSCredentials credentials = new BasicAWSCredentials(
            awsAccessKey, awsSecretKey
        );

        AmazonS3 s3Client =
            AmazonS3ClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion(Regions.fromName(awsRegion)).build();

        return s3Client;
    }
}
