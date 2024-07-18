package cz.fungisoft.test.image2.mappers;

import cz.fungisoft.test.image2.dto.ImageDto;
import cz.fungisoft.test.image2.dto.ImageObjectDto;
import cz.fungisoft.test.image2.entity.ImageObject;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@Mapper
public interface ImageObjectMapper {

    @Mapping(target = "baseBytesObjectUrl", expression = "java(getBaseBytesObjectUrl(io, baseBytesObjectUrlPath))")
    @Mapping(target = "baseBase64ObjectUrl", expression = "java(getBaseBase64ObjectUrl(io, baseBase64ObjectUrlPath))")
    @Mapping(target = "objectImages", expression = "java(mapImages(io, baseBytesImageUrlPath, baseBase64ImageUrlPath))")
    ImageObjectDto imageObjectToImageObjectDto(ImageObject io,
                                               String baseBytesObjectUrlPath, String baseBase64ObjectUrlPath,
                                               String baseBytesImageUrlPath, String baseBase64ImageUrlPath);

    default String getBaseBytesObjectUrl(ImageObject io, String baseBytesObjectUrlPath) {
        return buildBasePath(io, baseBytesObjectUrlPath);
    }

    default String getBaseBase64ObjectUrl(ImageObject io, String baseBase64ObjectUrlPath) {
        return buildBasePath(io, baseBase64ObjectUrlPath);
    }

    private String buildBasePath(ImageObject io, String pathToImage) {
        ServletUriComponentsBuilder builder = ServletUriComponentsBuilder.fromCurrentRequest();
        UriComponentsBuilder extBuilder = builder.scheme("https")
                .replacePath(pathToImage)
                .replaceQuery("objectExtId=" + io.getExternalObjectId())
                .port("8443");
        return extBuilder.build().toUriString();
    }

    default List<ImageDto> mapImages(ImageObject io, String baseBytesImageUrlPath, String baseBase64ImageUrlPath) {
        return io.getObjectImages().stream()
                                   .map(imageFileSet -> Mappers.getMapper(ImageMapper.class).imageFileSetToImageDto(imageFileSet, baseBytesImageUrlPath, baseBase64ImageUrlPath))
                                   .toList();
    }
}
