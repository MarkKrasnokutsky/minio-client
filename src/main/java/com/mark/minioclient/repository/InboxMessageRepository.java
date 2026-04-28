package com.mark.minioclient.repository;

import com.mark.minioclient.messaging.domain.entity.InboxMessage;
import com.mark.minioclient.messaging.domain.enumeration.InboxStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InboxMessageRepository extends JpaRepository<InboxMessage, String> {
    List<InboxMessage> findByStatus(InboxStatus status);
}
