package net.furizon.gallery_processor.utils.extractExif;

import com.drew.lang.annotations.NotNull;
import net.furizon.gallery_processor.dto.upload.GalleryProcessorUploadData;

public interface ExtractExif {
    void parseExif(@NotNull String path, @NotNull GalleryProcessorUploadData resultObj);
}
