package cz.fungisoft.test.image2.kafka;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ImageKafkaDto {

    private Integer id;

    @JsonFormat(pattern = "dd.MM. yyyy HH:mm", timezone="Europe/Prague")
    private LocalDateTime savedOn;

    @Size(max=512)
    private String fileName;

    public void setText(String fileName) {
        this.fileName = fileName.trim();
    }
}