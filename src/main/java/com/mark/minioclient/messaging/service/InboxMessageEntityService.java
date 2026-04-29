package com.mark.minioclient.messaging.service;

import com.mark.minioclient.domain.entity.FileEntity;
import com.mark.minioclient.domain.enumeration.FileStatus;
import com.mark.minioclient.messaging.domain.entity.InboxMessage;
import com.mark.minioclient.messaging.domain.enumeration.InboxStatus;
import com.mark.minioclient.repository.FileRepository;
import com.mark.minioclient.repository.InboxMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class InboxMessageEntityService {

    private final InboxMessageRepository inboxMessageRepository;
    private final FileRepository fileRepository;

    @Transactional
    public void saveResults(InboxMessage inboxMessage, String fullPath) {
        List<FileEntity> fileEntities = fileRepository.findByFilePath(fullPath);
        if (!fileEntities.isEmpty()) {
            fileEntities.forEach(fileEntity -> fileEntity.setStatus(FileStatus.SUCCESS));
            fileRepository.saveAll(fileEntities);
        }

        inboxMessage.setStatus(InboxStatus.PROCESSED);
        inboxMessage.setProcessedAt(LocalDateTime.now());
        save(inboxMessage);

        log.info("Successfully processed: {}", inboxMessage.getId());
    }

    public void save(String messageId, String valueRecord) {
        if (inboxMessageRepository.existsById(messageId)) {
            log.warn("Duplicate message id: {}", messageId);
            return;
        }

        InboxMessage inboxMessage = new InboxMessage();
        inboxMessage.setId(messageId);
        inboxMessage.setStatus(InboxStatus.PENDING);
        inboxMessage.setPayload(valueRecord);
        inboxMessage.setCreatedAt(LocalDateTime.now());

        inboxMessageRepository.save(inboxMessage);
    }

    public void save(InboxMessage inboxMessage) {
        inboxMessageRepository.save(inboxMessage);
    }

    public List<InboxMessage> findByStatus(InboxStatus status) {
        return inboxMessageRepository.findByStatus(status);
    }

}
