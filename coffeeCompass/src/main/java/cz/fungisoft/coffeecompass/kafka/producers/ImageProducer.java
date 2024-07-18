package cz.fungisoft.coffeecompass.kafka.producers;

import cz.fungisoft.coffeecompass.kafka.requests.ImageMessage;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.concurrent.CompletableFuture;

@Slf4j
@NoArgsConstructor
@Component
public class ImageProducer {

    final String productTopic = "images";

    private KafkaTemplate<String, Serializable> kafkaTemplate;

    @Autowired
    public ImageProducer(KafkaTemplate<String, Serializable> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void send(ImageMessage message) {
        CompletableFuture<SendResult<String, Serializable>> future = kafkaTemplate.send(productTopic, message);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Message sent successfully with offset = {}", result.getRecordMetadata().offset());
            }
            else {
                log.error("Unable to send message = {} dut to: {}", message.toString(), ex.getMessage());
            }
        });
    }

}
