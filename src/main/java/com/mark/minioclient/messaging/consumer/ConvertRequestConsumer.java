package com.mark.minioclient.messaging.consumer;

import com.mark.minioclient.messaging.service.InboxMessageEntityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConvertRequestConsumer {

    private final InboxMessageEntityService inboxMessageEntityService;

    @KafkaListener(topics = "${spring.kafka.topic.convert-response.name}",
            groupId = "${spring.kafka.consumer.group-id}")
    public void consume(ConsumerRecord<String, String> record) {
        String messageId = record.topic() + "-" + record.partition() + "-" + record.offset();

        inboxMessageEntityService.save(messageId, record.value());

        log.info("InboxMessage save success {}", messageId);
    }

}