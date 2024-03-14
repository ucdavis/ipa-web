package edu.ucdavis.dss.ipa.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class AWSConfiguration {
    @Value("${AWS_ACCESS_KEY}")
    String awsAccessKey;

    @Value("${AWS_SECRET_KEY}")
    String awsSecretKey;

    @Value("${AWS_REGION}")
    String awsRegion;

    @Bean
    public S3Client s3Client() {
        StaticCredentialsProvider credentialsProvider =
            StaticCredentialsProvider.create(AwsBasicCredentials.create(awsAccessKey, awsSecretKey));

        return S3Client.builder().credentialsProvider(credentialsProvider).region(Region.of(awsRegion)).build();
    }
}
