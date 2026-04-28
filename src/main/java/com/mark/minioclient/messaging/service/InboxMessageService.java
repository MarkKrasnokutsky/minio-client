package com.mark.minioclient.messaging.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mark.minioclient.config.MinioConfig;
import com.mark.minioclient.domain.entity.File;
import com.mark.minioclient.domain.enumeration.FileStatus;
import com.mark.minioclient.messaging.domain.ConvertRequestMessage;
import com.mark.minioclient.messaging.domain.entity.InboxMessage;
import com.mark.minioclient.messaging.domain.enumeration.InboxStatus;
import com.mark.minioclient.repository.FileRepository;
import com.mark.minioclient.utils.FileUtils;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class InboxMessageService {

    private final ObjectMapper objectMapper;
    private final InboxMessageEntityService inboxMessageEntityService;
    private final FileRepository fileRepository;
    private final MinioConfig minioConfig;

    @Value("${minio.bucketName}")
    private String bucketName;

    public void processMessages() {
        MinioClient client = minioConfig.createClient();
        List<InboxMessage> pendingMessages = inboxMessageEntityService.findByStatus(InboxStatus.PENDING);

        for (InboxMessage inboxMessage : pendingMessages) {
            try {
                String json = objectMapper.readValue(inboxMessage.getPayload(), String.class); // сначала разворачиваем строку

                ConvertRequestMessage request = objectMapper.readValue(json, ConvertRequestMessage.class); // потом парсим объект
                String fullPath = FileUtils.removeExtension(request.getPath());

                InputStream stream = client.getObject(
                        GetObjectArgs.builder()
                                .bucket(bucketName)
                                .object(fullPath + ".pdf")
                                .build()
                );
                byte[] bytes = stream.readAllBytes();

                if (bytes.length > 0) {
                    List<File> files = fileRepository.findByFilePath(fullPath);

                    if (!files.isEmpty()) {
                        files.forEach(file -> {
                            file.setStatus(FileStatus.SUCCESS);
                        });
                        fileRepository.saveAll(files);
                    }
                }

                inboxMessage.setStatus(InboxStatus.PROCESSED);
                inboxMessageEntityService.save(inboxMessage);
                log.info("Successfully processed: {}", inboxMessage.getId());
            } catch (Exception e) {
                log.error("Failed processing InboxMessage {}", inboxMessage, e);
                inboxMessage.setStatus(InboxStatus.FAILED);
                inboxMessageEntityService.save(inboxMessage);
            }
        }
    }

}
