package net.furizon.gallery_processor.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

@AllArgsConstructor
public enum JobType {
    IMGAGE(ImageResponse.class),
    VIDEO(VideoResponse.class);

    @Getter
    @NotNull
    private final Class<?> responseType;
}
