package cz.fungisoft.test.image2.serviceimpl;

import cz.fungisoft.test.image2.kafka.ImageMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class KafkaImageConsumer {

    @KafkaListener(topics = "images", containerFactory = "kafkaListenerContainerFactory")
    public void newProductListener(ImageMessage product) {
        log.info("Get request from images topic " + product.toString());
    }
}
