package net.furizon.gallery_processor.utils.extractImageMetadata;

import com.drew.imaging.FileType;
import com.drew.lang.annotations.NotNull;
import net.furizon.gallery_processor.dto.upload.GalleryProcessorUploadData;

public interface ExtractImageMetadata {
    void parseExif(@NotNull String path, @NotNull GalleryProcessorUploadData resultObj, @NotNull FileType fileType);
}
