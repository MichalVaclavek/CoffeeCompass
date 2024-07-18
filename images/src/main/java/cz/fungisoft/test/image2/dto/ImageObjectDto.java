package cz.fungisoft.test.image2.dto;

import lombok.Data;

import java.util.List;

@Data
public class ImageObjectDto {

    private String externalObjectId;

    private String baseBytesObjectUrl;

    private String baseBase64ObjectUrl;

    private List<ImageDto> objectImages;
}
