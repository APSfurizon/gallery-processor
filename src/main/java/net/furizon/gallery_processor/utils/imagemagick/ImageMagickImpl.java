package net.furizon.gallery_processor.utils.imagemagick;

import com.drew.imaging.FileType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.gallery_processor.dto.upload.GalleryProcessorUploadData;
import net.furizon.gallery_processor.utils.cmdExecutor.CmdExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class ImageMagickImpl implements ImageMagick {

    @NotNull
    private final CmdExecutor cmdExecutor;

    @Nullable
    @Value("${cmd-executor.imagemagick-binary}")
    private String imagemagickBinary;

    @Value("${worker.thumbnail.max-dimension}")
    private int thumbnailMaxDimension;
    @Value("${worker.thumbnail.min-dimension}")
    private int thumbnailMinDimension;

    @NotNull
    @Value("${worker.quality}")
    private Integer quality = 1;

    public static final Set<FileType> PREFIX_NEEDED_FILE_TYPES = Set.of(
        FileType.Cr2
    );

    @NotNull
    private String getFullSourceFIle(@NotNull Path file, @NotNull FileType fileType) {
        return PREFIX_NEEDED_FILE_TYPES.contains(fileType) ? fileType.getName().toLowerCase() + ":" : "" + file.toAbsolutePath().toString();
    }

    @Override
    public void renderToWebp(@NotNull Path source, @NotNull Path render, @NotNull FileType originalFileType) throws IOException {
        log.info("Rendering image from {} to {}", source, render);
        cmdExecutor.execute(
            "IMAGEMAGICK_RENDERTOWEBP",
            imagemagickBinary == null ? "convert" : imagemagickBinary,
            getFullSourceFIle(source, originalFileType),
            "-alpha", "off",
            "-quality", quality.toString(),
            "-define", "webp:preserve-metadata=all",
            render.toAbsolutePath().toString()
        );
    }

    @Override
    public void getWidthAndHeight(@NotNull Path source, @NotNull FileType originalFileType, @NotNull GalleryProcessorUploadData data) throws IOException {
        log.info("Getting width and height from {}", source);
        String size = imagemagickBinary == null ?
            cmdExecutor.execute(
                "IMAGEMAGICK_GETWIDTHEIGHT",
                "identify",
                "-format", "\"%wx%h\"",
                getFullSourceFIle(source, originalFileType)
            ) :
            cmdExecutor.execute(
                "IMAGEMAGICK_GETWIDTHEIGHT",
                imagemagickBinary,
                "identify",
                "-format", "\"%wx%h\"",
                getFullSourceFIle(source, originalFileType)
            );
        String sp[] = size.split("x");
        if (data.getResolutionWidth() == 0) data.setResolutionWidth(Integer.parseInt(sp[0]));
        if (data.getResolutionHeight() == 0) data.setResolutionHeight(Integer.parseInt(sp[1]));
    }

    @Override
    public void createThumbnail(@NotNull Path source, @NotNull Path dest, int width, int height, @NotNull FileType originalFileType) throws IOException {
        log.info("Creating thumbnail from {} to {}", source, dest);
        /*cmdExecutor.execute(
                "IMAGEMAGICK_THUMBNAIL",
                imagemagickBinary == null ? "convert" : imagemagickBinary,
                getFullSourceFIle(source, originalFileType),
                "-resize", "400x400^>",       // do not upscale small images
                "-gravity", "center",
                "-background", "white",
                "-extent", "400x400",         // pad if needed
                "-alpha", "off",
                "-quality", "85",
                "-define", "webp:preserve-metadata=all",
                dest.toAbsolutePath().toString()
        );*/
        int dimension = Math.max(Math.min(Math.min(width, height),  thumbnailMaxDimension),  thumbnailMinDimension);
        String dimStr = String.valueOf(dimension);
        cmdExecutor.execute(
                "IMAGEMAGICK_THUMBNAIL",
                imagemagickBinary == null ? "convert" : imagemagickBinary,
                getFullSourceFIle(source, originalFileType),
                "-resize", dimStr + "x" + dimStr + "^",
                "-gravity", "center",
                "-crop", dimStr + "x" + dimStr + "+0+0", "+repage",
                "-alpha", "off",
                "-quality", quality.toString(),
                "-define", "webp:preserve-metadata=all",
                dest.toAbsolutePath().toString()
        );
    }
}


