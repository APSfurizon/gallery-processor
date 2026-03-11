package net.furizon.gallery_processor.utils.ffmpeg;

import com.drew.imaging.FileType;
import net.furizon.gallery_processor.dto.upload.GalleryProcessorUploadData;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;

public interface Ffmpeg {
    void extractMetadata(@NotNull Path file, @NotNull FileType fileType, @NotNull GalleryProcessorUploadData data) throws IOException;

    void extractFirstFrame(@NotNull Path source, @NotNull Path dest, @NotNull FileType fileType, int duration) throws IOException;
}
