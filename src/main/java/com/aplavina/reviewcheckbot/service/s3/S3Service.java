package com.aplavina.reviewcheckbot.service.s3;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

@Slf4j
@Service
public class S3Service {
    private final String bucketName;
    private final AmazonS3 s3Client;

    public S3Service(@Value("${s3.bucket}") String bucketName, AmazonS3 s3Client) {
        this.bucketName = bucketName;
        this.s3Client = s3Client;
    }

    public void uploadFile(InputStream fileStream, String type, String key) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            fileStream.transferTo(outputStream);
            byte[] bytes = outputStream.toByteArray();
            ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
            ObjectMetadata meta = new ObjectMetadata();
            meta.setContentType(type);
            meta.setContentLength(bytes.length);
            PutObjectRequest putObject = new PutObjectRequest(bucketName, key, inputStream, meta);
            s3Client.putObject(putObject);
        } catch (Exception e) {
            log.error("Failed to upload file to S3", e);
        }
    }

    public InputStream getFileInputStream(String key) {
        try {
            S3Object s3Object = s3Client.getObject(bucketName, key);
            return s3Object.getObjectContent();
        } catch (Exception e) {
            log.error("Failed to download file from S3 with key: {}", key, e);
            throw new IllegalStateException("Failed to download file from S3");
        }
    }
}
