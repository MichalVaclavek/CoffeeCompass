package cz.fungisoft.test.image2.kafka;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class ImageMessage extends ImageKafkaDto implements Serializable {

    String action;

    public ImageMessage(ImageKafkaDto p, String action) {
        this.setId(p.getId());
        this.setFileName(p.getFileName());
        this.setSavedOn(p.getSavedOn());
        this.action = action;
    }
}
