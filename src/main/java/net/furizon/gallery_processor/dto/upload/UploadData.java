package net.furizon.gallery_processor.dto.upload;

import lombok.experimental.SuperBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;

@SuperBuilder
public class UploadData {
    private int resolutionWidth;
    private int resolutionHeight;
    @Nullable private LocalDateTime shotTimestamp;

    private long hash;
    private long fileSize;
    @NotNull private String mimeType;
}
