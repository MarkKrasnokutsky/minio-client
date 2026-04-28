package com.mark.minioclient.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mark.minioclient.config.MinioConfig;
import com.mark.minioclient.domain.dto.FileResponse;
import com.mark.minioclient.domain.entity.File;
import com.mark.minioclient.domain.enumeration.FileStatus;
import com.mark.minioclient.exception.FileNotFoundException;
import com.mark.minioclient.exception.ManageFileException;
import com.mark.minioclient.messaging.domain.ConvertRequestMessage;
import com.mark.minioclient.messaging.domain.entity.OutboxMessage;
import com.mark.minioclient.messaging.domain.enumeration.OutboxStatus;
import com.mark.minioclient.messaging.producer.ConvertRequestProducer;
import com.mark.minioclient.messaging.service.OutboxMessageEntityService;
import com.mark.minioclient.repository.FileRepository;
import com.mark.minioclient.service.FileService;
import com.mark.minioclient.utils.FileUtils;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileServiceImpl implements FileService {

    private final MinioConfig minioConfig;

    private final FileRepository fileRepository;

    private final ObjectMapper objectMapper;

    private final OutboxMessageEntityService outboxMessageEntityService;

    @Value("${minio.bucketName}")
    private String bucketName;

    @Value("${minio.filesPath}")
    private String filesPath;

    @Override
    public String upload(MultipartFile file) {
        MinioClient client = minioConfig.createClient();
        try {
            byte[] fileBytes = file.getBytes();
            String currentPath = filesPath + "/" + file.getOriginalFilename();
            try (ByteArrayInputStream inputStream = new ByteArrayInputStream(file.getBytes())) {
                client.putObject(
                        PutObjectArgs.builder()
                                .bucket(bucketName)
                                .object(currentPath)
                                .stream(inputStream, fileBytes.length, -1)
                                .contentType(file.getContentType())
                                .build()
                );
                log.info("Uploaded file of {} bytes to {}", fileBytes.length, currentPath);

                fileRepository.save(File.builder()
                        .data(fileBytes)
                        .filePath(currentPath)
                        .fileName(file.getOriginalFilename())
                        .contentType(file.getContentType())
                        .createdAt(LocalDateTime.now())
                        .status(FileStatus.PENDING)
                        .build());

                OutboxMessage outboxMessage = new OutboxMessage();
                outboxMessage.setTopic("convert-request");
                outboxMessage.setStatus(OutboxStatus.PENDING);
                outboxMessage.setCreatedAt(LocalDateTime.now());
                outboxMessage.setPayload(objectMapper.writeValueAsString(
                        ConvertRequestMessage.builder()
                                .path(currentPath)
                                .bucketName(bucketName)
                                .fileName(file.getOriginalFilename())
                                .typeFormat(file.getContentType())
                                .build()
                ));

                outboxMessageEntityService.save(outboxMessage);
            }
        } catch (Exception e) {
            throw new ManageFileException("Failed to upload PDF: " + filesPath, e);
        }
        return "The file has been sent to the repository";
    }

    @Override
    public FileStatus getFileStatusById(Long id) {
        return fileRepository
                .findById(id)
                .orElseThrow(() ->
                        new FileNotFoundException("File with id " + id + " not found"))
                .getStatus();
    }



    @Override
    public byte[] getFileData(Long id) {
        File file = fileRepository.findById(id).orElseThrow(() -> new FileNotFoundException("File with id " + id + " not found"));
        MinioClient client = minioConfig.createClient();

        String modifiedPath = FileUtils.removeExtension(file.getFilePath());

        try {
            InputStream stream = client.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(modifiedPath + ".pdf")
                            .build()
            );
            return stream.readAllBytes();
        } catch (Exception e) {
            throw new ManageFileException("Failed to get PDF file data from the repository", e);
        }
    }

    @Override
    public FileResponse getFileInfo(Long id) {
        File file = fileRepository.findById(id).orElseThrow(() -> new FileNotFoundException("File with id " + id + " not found"));
        return FileResponse.builder()
                .id(file.getId())
                .fileData(file.getData())
                .fileStatus(file.getStatus())
                .contentType(file.getContentType())
                .fileName(file.getFileName())
                .build();
    }


}
