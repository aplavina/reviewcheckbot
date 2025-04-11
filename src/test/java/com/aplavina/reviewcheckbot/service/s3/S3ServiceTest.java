package com.aplavina.reviewcheckbot.service.s3;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class S3ServiceTest {
    private static final String BUCKET_NAME = "test-bucket";

    private S3Service s3Service;
    private AmazonS3 amazonS3;

    @BeforeEach
    void setUp() {
        amazonS3 = mock(AmazonS3.class);
        s3Service = new S3Service(BUCKET_NAME, amazonS3);
    }

    @Test
    void testUploadFile() {
        String key = "test-file.txt";
        String contentType = "text/plain";
        byte[] data = "Hello, S3!".getBytes();
        InputStream inputStream = new ByteArrayInputStream(data);

        s3Service.uploadFile(inputStream, contentType, key);

        ArgumentCaptor<PutObjectRequest> captor = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(amazonS3, times(1)).putObject(captor.capture());

        PutObjectRequest request = captor.getValue();
        assertEquals(BUCKET_NAME, request.getBucketName());
        assertEquals(key, request.getKey());
        assertEquals(contentType, request.getMetadata().getContentType());
        assertEquals(data.length, request.getMetadata().getContentLength());
    }

    @Test
    void testGetFileInputStream() {
        String key = "download-key.txt";
        byte[] data = "Downloaded content".getBytes();
        S3Object s3Object = new S3Object();
        s3Object.setObjectContent(new S3ObjectInputStream(
                new ByteArrayInputStream(data), null));

        when(amazonS3.getObject(BUCKET_NAME, key)).thenReturn(s3Object);

        InputStream result = s3Service.getFileInputStream(key);
        assertNotNull(result);
        verify(amazonS3, times(1)).getObject(BUCKET_NAME, key);
    }

    @Test
    void testGetFileInputStreamThrows() {
        String key = "missing-file.txt";
        when(amazonS3.getObject(BUCKET_NAME, key)).thenThrow(new RuntimeException("Not found"));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            s3Service.getFileInputStream(key);
        });

        assertEquals("Failed to download file from S3", exception.getMessage());
    }
}