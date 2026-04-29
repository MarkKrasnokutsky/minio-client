package com.mark.minioclient.repository;

import com.mark.minioclient.messaging.domain.entity.OutboxMessage;
import com.mark.minioclient.messaging.domain.enumeration.OutboxStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OutboxMessageRepository extends JpaRepository<OutboxMessage, String> {
    List<OutboxMessage> findByStatus(OutboxStatus status);
}
