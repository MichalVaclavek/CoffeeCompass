package cz.fungisoft.test.image2.mappers;

import cz.fungisoft.test.image2.dto.ImageDto;
import cz.fungisoft.test.image2.entity.ImageFileSet;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Mapper
public interface ImageMapper {

    @Mapping(target = "baseBytesImageUrl", expression = "java(getBaseBytesImageUrl(imageFileSet,baseBytesImageUrlPath))")
    @Mapping(target = "baseBase64ImageUrl", expression = "java(getBaseBase64ImageUrl(imageFileSet,baseBase64ImageUrlPath))")
    @Mapping(target = "externalId", source = "imageFileSet.extId")
    @Mapping(target = "savedOn", source = "imageFileSet.savedOn", qualifiedByName = "mapSavedOn")
    ImageDto imageFileSetToImageDto(ImageFileSet imageFileSet, String baseBytesImageUrlPath, String baseBase64ImageUrlPath);

    default String getBaseBytesImageUrl(ImageFileSet ifs, String baseBytesImageUrlPath) {
        return buildBasePath(ifs, baseBytesImageUrlPath);
    }

    default String getBaseBase64ImageUrl(ImageFileSet ifs, String baseBase64ImageUrlPath) {
        return buildBasePath(ifs, baseBase64ImageUrlPath);
    }

    private String buildBasePath(ImageFileSet ifs, String pathToImage) {
        ServletUriComponentsBuilder builder = ServletUriComponentsBuilder.fromCurrentRequest();
        UriComponentsBuilder extBuilder = builder.scheme("https")
                .replacePath(pathToImage)
                .replaceQuery("imageExtId=" + ifs.getExtId() )
                .port("8443");
        return extBuilder.build().toUriString();
    }

    // convert source LocalDateTime savedOn to target OffsetDateTime savedOn. Zone is UTC +1 hour
    @Named("mapSavedOn")
    default OffsetDateTime mapSavedOn(LocalDateTime savedOn) {
        return savedOn == null ? null : OffsetDateTime.of(savedOn, ZoneOffset.ofHours(1));
    }
}
