package cz.fungisoft.test.image2.mappers;

import cz.fungisoft.test.image2.configuration.ConfigProperties;
import cz.fungisoft.test.image2.dto.ImageDto;
import cz.fungisoft.test.image2.dto.ImageObjectDto;
import cz.fungisoft.test.image2.entity.ImageObject;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@Mapper
public abstract class ImageObjectMapper {

    private ConfigProperties fileStorageProperties = null;

    @Autowired
    public void setService(ConfigProperties fileStorageProperties) {
        this.fileStorageProperties = fileStorageProperties;
    }

    @Mapping(target = "baseBytesObjectUrl", expression = "java(getBaseBytesObjectUrl(io, baseBytesObjectUrlPath))")
    @Mapping(target = "baseBase64ObjectUrl", expression = "java(getBaseBase64ObjectUrl(io, baseBase64ObjectUrlPath))")
    @Mapping(target = "objectImages", expression = "java(mapImages(io, baseBytesImageUrlPath, baseBase64ImageUrlPath))")
    public abstract ImageObjectDto imageObjectToImageObjectDto(ImageObject io,
                                               String baseBytesObjectUrlPath, String baseBase64ObjectUrlPath,
                                               String baseBytesImageUrlPath, String baseBase64ImageUrlPath);

    String getBaseBytesObjectUrl(ImageObject io, String baseBytesObjectUrlPath) {
        return buildBasePath(io, baseBytesObjectUrlPath);
    }

    String getBaseBase64ObjectUrl(ImageObject io, String baseBase64ObjectUrlPath) {
        return buildBasePath(io, baseBase64ObjectUrlPath);
    }

    private String buildBasePath(ImageObject io, String pathToImage) {
        ServletUriComponentsBuilder builder = ServletUriComponentsBuilder.fromCurrentRequest();
        UriComponentsBuilder extBuilder = builder.scheme("https")
                // host must be replaced to coffeecompass, if running in Docker container
//                .host(fileStorageProperties.getImagesHost())
                .replacePath(pathToImage)
                .replaceQuery("objectExtId=" + io.getExternalObjectId())
                .port("8443");
        return extBuilder.build().toUriString();
    }

    List<ImageDto> mapImages(ImageObject io, String baseBytesImageUrlPath, String baseBase64ImageUrlPath) {
        return io.getObjectImages().stream()
                                   .map(imageFileSet -> Mappers.getMapper(ImageMapper.class).imageFileSetToImageDto(imageFileSet, baseBytesImageUrlPath, baseBase64ImageUrlPath))
                                   .toList();
    }
}
