package net.furizon.gallery_processor.dto.upload;

import com.drew.imaging.FileType;
import lombok.Builder;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.util.MimeTypeUtils;
import software.amazon.awssdk.core.internal.util.Mimetype;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
public class GalleryProcessorUploadData {
    private int resolutionWidth;
    private int resolutionHeight;
    @Nullable private OffsetDateTime shotTimestamp;

    private UUID hash;
    private long fileSize;
    @NotNull private String mimeType;

    @Builder.Default
    @NotNull private String extraMediaMimeType = FileType.WebP.getMimeType();
    @Nullable private String thumbnailMediaName; //This should be not null tho!
    @Nullable private String renderedMediaName;

    @Nullable private UploadImageMetadata photoMetadata;
    @Nullable private UploadVideoMetadata videoMetadata;
}
