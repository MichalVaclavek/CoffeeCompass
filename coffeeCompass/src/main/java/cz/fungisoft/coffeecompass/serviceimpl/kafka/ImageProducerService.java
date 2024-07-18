package cz.fungisoft.coffeecompass.serviceimpl.kafka;

import cz.fungisoft.coffeecompass.kafka.producers.ImageProducer;
import cz.fungisoft.coffeecompass.kafka.requests.ImageMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ImageProducerService {

    private ImageProducer productProducer;

    public ImageProducerService(ImageProducer productProducer) {
        this.productProducer = productProducer;
    }

    public void sendMessage(ImageMessage message) {
        log.info("[ImageProducerService] send image info to topic");
        productProducer.send(message);
    }
}
