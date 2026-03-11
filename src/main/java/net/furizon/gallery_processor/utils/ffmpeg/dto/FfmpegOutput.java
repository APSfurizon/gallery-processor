package net.furizon.gallery_processor.utils.ffmpeg.dto;

import lombok.Data;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Data
public class FfmpegOutput {
    //Omitting programs

    @Nullable
    private final List<FfmpegStream> streams;

    @Nullable
    private final FfmpegFormat format;
}
