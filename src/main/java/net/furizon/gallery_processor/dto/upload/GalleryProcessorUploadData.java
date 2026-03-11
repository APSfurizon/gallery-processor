package net.furizon.gallery_processor.dto.upload;

import lombok.Builder;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class GalleryProcessorUploadData {
    private int resolutionWidth;
    private int resolutionHeight;
    @Nullable private LocalDateTime shotTimestamp;

    private UUID hash;
    private long fileSize;
    @NotNull private String mimeType;

    @Nullable private String thumbnailMediaName; //This should be not null tho!
    @Nullable private String renderedMediaName;

    @Nullable private UploadImageMetadata photoMetadata;
    @Nullable private UploadVideoMetadata videoMetadata;
}
