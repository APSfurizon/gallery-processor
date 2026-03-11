package net.furizon.gallery_processor.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import net.furizon.gallery_processor.dto.upload.UploadExif;
import net.furizon.gallery_processor.dto.upload.UploadVideo;
import org.jetbrains.annotations.NotNull;

@AllArgsConstructor
public enum JobType {
    IMGAGE(UploadExif.class),
    VIDEO(UploadVideo.class),
    UNKNOWN(Void.class),;

    @Getter
    @NotNull
    private final Class<?> responseType;
}
