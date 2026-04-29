package com.mark.minioclient.service.impl;

import com.mark.minioclient.config.MinioConfig;
import com.mark.minioclient.exception.ManageFileException;
import com.mark.minioclient.service.MinioService;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class MinioServiceImpl implements MinioService {

    private final MinioConfig minioConfig;

    @Override
    public byte[] getFile(String bucketName, String filePath) {
        MinioClient client = minioConfig.createClient();
        try {
            InputStream stream = client.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(filePath + ".pdf")
                            .build()
            );
            return stream.readAllBytes();
        } catch (Exception e) {
            throw new ManageFileException("Failed to get PDF file data from the repository", e);
        }
    }

    @Override
    public void uploadFile(MultipartFile file, String bucketName, String filePath) {
        MinioClient client = minioConfig.createClient();
        try {
            byte[] fileBytes = file.getBytes();

            try (ByteArrayInputStream inputStream = new ByteArrayInputStream(file.getBytes())) {
                client.putObject(
                        PutObjectArgs.builder()
                                .bucket(bucketName)
                                .object(filePath)
                                .stream(inputStream, fileBytes.length, -1)
                                .contentType(file.getContentType())
                                .build()
                );
                log.info("Uploaded file of {} bytes to {}", fileBytes.length, filePath);
            }
        } catch (Exception e) {
            throw new ManageFileException("Failed to upload PDF: " + filePath, e);
        }
    }
}
