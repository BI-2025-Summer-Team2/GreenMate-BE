package kr.bi.greenmate.common.domain;

import java.util.Arrays;
import java.util.Optional;

public enum ImageFileExtension {
    JPG("image/jpeg"), JPEG("image/jpeg"), PNG("image/png"), GIF("image/gif");

    private final String mimeType;

    ImageFileExtension(String mimeType){
        this.mimeType = mimeType;
    }

    String getMimeType(){
        return mimeType;
    }

    public static Optional<ImageFileExtension> fromExtension(String extension){
        if(extension == null || extension.isBlank()){
            return Optional.empty();
        }

        return Arrays.stream(values())
                .filter(e -> e.name().equalsIgnoreCase(extension))
                .findFirst();
    }

    public static boolean isAllowedMimeType(String mimeType) {
        if (mimeType == null || mimeType.isBlank()) {
            return false;
        }

        return Arrays.stream(ImageFileExtension.values())
                .anyMatch(e ->
                        e.getMimeType().equalsIgnoreCase(mimeType));
    }
}
