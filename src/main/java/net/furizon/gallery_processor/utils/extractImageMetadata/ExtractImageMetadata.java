package net.furizon.gallery_processor.utils.extractImageMetadata;

import com.drew.imaging.FileType;
import com.drew.lang.annotations.NotNull;
import net.furizon.gallery_processor.dto.upload.GalleryProcessorUploadData;

import java.nio.file.Path;

public interface ExtractImageMetadata {
    void parseExif(@NotNull Path file, @NotNull GalleryProcessorUploadData resultObj, @NotNull FileType fileType);
}
