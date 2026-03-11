package net.furizon.gallery_processor.utils.extractFileType;

import com.drew.imaging.FileType;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;

public interface ExtractFileType {
    @NotNull
    FileType invoke(@NotNull Path file) throws IOException;
}
