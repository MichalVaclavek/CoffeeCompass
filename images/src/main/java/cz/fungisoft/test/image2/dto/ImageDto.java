package cz.fungisoft.test.image2.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ImageDto {

    private String externalId;

    private String imageType;

    private String description;

    @JsonFormat(pattern = "dd.MM.yyyy HH:mm", timezone="Europe/Prague")
    private LocalDateTime savedOn;

    private String baseBytesImageUrl;

    private String baseBase64ImageUrl;
}
