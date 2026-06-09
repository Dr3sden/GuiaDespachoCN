package cl.duoc.guias_despacho.service;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.File;

@Service
public class S3Service {

    private final S3Client s3;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    public S3Service() {
        this.s3 = S3Client.builder()
                .region(Region.US_EAST_1)
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }

    public void uploadFile(String key, File file) {
        s3.putObject(PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build(), file.toPath());
    }

    public void deleteFile(String key) {
        s3.deleteObject(DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build());
    }

    public File downloadFile(String key, File destination) {
        s3.getObject(GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build(), destination.toPath());
        return destination;
    }
}