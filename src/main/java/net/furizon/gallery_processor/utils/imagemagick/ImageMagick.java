package net.furizon.gallery_processor.utils.imagemagick;

import com.drew.imaging.FileType;
import net.furizon.gallery_processor.dto.upload.GalleryProcessorUploadData;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;

public interface ImageMagick {
    void renderToWebp(@NotNull Path source, @NotNull Path render, @NotNull FileType originalFileType) throws IOException;

    void getWidthAndHeight(@NotNull Path source, @NotNull FileType originalFileType, @NotNull GalleryProcessorUploadData data) throws IOException;

    void createThumbnail(@NotNull Path source, @NotNull Path dest, int width, int height, @NotNull FileType originalFileType) throws IOException;
}
