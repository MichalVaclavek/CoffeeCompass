package cz.fungisoft.test.image2.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Data
public class ImageDto {

    private String externalId;

    private String imageType;

    private String description;

//    @JsonFormat(pattern = "yyyy-MM-ddTHH:mm:ssZ", timezone="Europe/Prague") // to be valid OffsetDateTime on client side. The respective Swagger plugin should generate the same format
//    @JsonFormat(pattern = "yyyy-MM-ddTHH:mm:ssZ")
//    private LocalDateTime savedOn;
    private OffsetDateTime savedOn;

    private String baseBytesImageUrl;

    private String baseBase64ImageUrl;
}
