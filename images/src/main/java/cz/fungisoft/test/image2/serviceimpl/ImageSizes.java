package cz.fungisoft.test.image2.serviceimpl;

import java.util.Arrays;
import java.util.Optional;

public enum ImageSizes {

    HD("hd"),
    ORIGINAL("original"),
    SMALL("small"),
    MID("mid"),
    LARGE("large");

    public final String size;

    ImageSizes(String size) {
        this.size = size;
    }

    public static Optional<ImageSizes> get(String size) {
        return Arrays.stream(ImageSizes.values())
                .filter(s -> s.size.equalsIgnoreCase(size))
                .findFirst();
    }
}
