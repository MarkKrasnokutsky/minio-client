package com.mark.minioclient.messaging.producer;

import com.mark.minioclient.messaging.domain.ConvertRequestMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ConvertRequestProducer {

    private final KafkaTemplate<String, ConvertRequestMessage> kafkaTemplate;

    public void send(ConvertRequestMessage message) {
        kafkaTemplate.send("convert-request", message);
    }
}