package cz.fungisoft.coffeecompass.controller.kafka;

import cz.fungisoft.coffeecompass.dto.ImageKafkaDTO;
import cz.fungisoft.coffeecompass.kafka.requests.ImageMessage;
import cz.fungisoft.coffeecompass.serviceimpl.kafka.ImageProducerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/kafka")
@Slf4j
@RequiredArgsConstructor
public class KafkaController {

    private final ImageProducerService imageProducerService;

    @PostMapping("/image")
    public ResponseEntity<ImageKafkaDTO> addProduct(@RequestBody ImageKafkaDTO image) {
        log.info("[DemoController]: add new image = " + image.toString());
        this.imageProducerService.sendMessage(new ImageMessage(image, "add"));
        return ResponseEntity.ok(image);
    }

    @DeleteMapping("/product/{id}")
    void deleteProduct(@PathVariable String id) {
        log.info("[DemoController]: delete image id = " + id);
        ImageKafkaDTO p = new ImageKafkaDTO();
        p.setId(Integer.parseInt(id));
        this.imageProducerService.sendMessage(new ImageMessage(p, "delete"));
    }

}
