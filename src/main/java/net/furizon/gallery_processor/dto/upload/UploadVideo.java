package net.furizon.gallery_processor.dto.upload;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import org.jetbrains.annotations.Nullable;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class UploadVideo extends UploadData {
    @Nullable private String audioFrequency;
    @Nullable private String videoCodec;
    @Nullable private String audioCodec;
    @Nullable private String framerate;
    private int length;
}
