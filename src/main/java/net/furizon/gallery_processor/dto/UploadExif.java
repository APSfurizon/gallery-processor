package net.furizon.gallery_processor.dto;

import lombok.Builder;
import lombok.Data;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;

@Data
@Builder
public class UploadExif {
    @Nullable private final String cameraMaker;
    @Nullable private final String cameraModel;
    @Nullable private final String lensMaker;
    @Nullable private final String lensModel;
    @Nullable private final String focal;
    @Nullable private final String shutter;
    @Nullable private final String aperture;
    @Nullable private final String iso;
    @Nullable private final LocalDateTime shotTimestamp;
}
