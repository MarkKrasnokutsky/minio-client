package com.mark.minioclient.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mark.minioclient.domain.dto.FileResponse;
import com.mark.minioclient.domain.entity.FileEntity;
import com.mark.minioclient.domain.enumeration.FileStatus;
import com.mark.minioclient.exception.FileNotFoundException;
import com.mark.minioclient.messaging.domain.ConvertRequestMessage;
import com.mark.minioclient.messaging.domain.entity.OutboxMessage;
import com.mark.minioclient.messaging.domain.enumeration.OutboxStatus;
import com.mark.minioclient.messaging.service.OutboxMessageEntityService;
import com.mark.minioclient.repository.FileRepository;
import com.mark.minioclient.service.FileService;
import com.mark.minioclient.service.MinioService;
import com.mark.minioclient.utils.FileUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileServiceImpl implements FileService {

    private final FileRepository fileRepository;

    private final ObjectMapper objectMapper;

    private final OutboxMessageEntityService outboxMessageEntityService;

    private final MinioService minioService;

    @Value("${minio.bucketName}")
    private String bucketName;

    @Value("${minio.filesPath}")
    private String filesPath;

    @Override
    public void upload(MultipartFile file) throws IOException {
        String currentPath = filesPath + "/" + file.getOriginalFilename();
        byte[] fileBytes = file.getBytes();

        fileRepository.save(FileEntity.builder()
                .data(fileBytes)
                .filePath(currentPath)
                .fileName(file.getOriginalFilename())
                .contentType(file.getContentType())
                .createdAt(LocalDateTime.now())
                .status(FileStatus.PENDING)
                .build());

        minioService.uploadFile(file, bucketName, currentPath);

        OutboxMessage outboxMessage = new OutboxMessage();
        outboxMessage.setTopic("convert-request");
        outboxMessage.setStatus(OutboxStatus.PENDING);
        outboxMessage.setCreatedAt(LocalDateTime.now());
        try {
            outboxMessage.setPayload(objectMapper.writeValueAsString(
                    ConvertRequestMessage.builder()
                            .path(currentPath)
                            .bucketName(bucketName)
                            .fileName(file.getOriginalFilename())
                            .typeFormat(file.getContentType())
                            .build()
            ));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        outboxMessageEntityService.save(outboxMessage);
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
        FileEntity fileEntity = fileRepository.findById(id).orElseThrow(() -> new FileNotFoundException("File with id " + id + " not found"));

        String modifiedPath = FileUtils.removeExtension(fileEntity.getFilePath());

        return minioService.getFile(bucketName, modifiedPath);
    }

    @Override
    public FileResponse getFileInfo(Long id) {
        FileEntity fileEntity = fileRepository.findById(id).orElseThrow(() -> new FileNotFoundException("File with id " + id + " not found"));
        return FileResponse.builder()
                .id(fileEntity.getId())
                .fileData(fileEntity.getData())
                .fileStatus(fileEntity.getStatus())
                .contentType(fileEntity.getContentType())
                .fileName(fileEntity.getFileName())
                .build();
    }


}
