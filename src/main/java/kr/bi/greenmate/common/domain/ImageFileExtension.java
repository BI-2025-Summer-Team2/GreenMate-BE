package kr.bi.greenmate.common.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Optional;

@Getter
@RequiredArgsConstructor
public enum ImageFileExtension {
    JPG("image/jpeg"),
    JPEG("image/jpeg"),
    PNG("image/png"),
    GIF("image/gif"),
    WEBP("image/webp"),
    AVIF("image/avif");

    private final String mimeType;

    public static Optional<ImageFileExtension> fromExtension(String extension) {
        if (extension == null || extension.isBlank()) {
            return Optional.empty();
        }

        return Arrays.stream(values())
                .filter(e -> e.name().equals(extension))
                .findFirst();
    }
}
