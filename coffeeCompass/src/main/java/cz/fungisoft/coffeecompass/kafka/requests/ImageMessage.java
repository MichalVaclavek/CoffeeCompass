package cz.fungisoft.coffeecompass.kafka.requests;

import cz.fungisoft.coffeecompass.dto.ImageKafkaDTO;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class ImageMessage extends ImageKafkaDTO implements Serializable {

    String action;

    public ImageMessage(ImageKafkaDTO p, String action) {
        this.setId(p.getId());
        this.setFileName(p.getFileName());
        this.setSavedOn(p.getSavedOn());
        this.action = action;
    }
}
