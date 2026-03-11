package net.furizon.gallery_processor.utils.hashFile;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;
import java.util.UUID;

public interface HashFile {
    @NotNull
    UUID hashFile(@NotNull Path file) throws IOException;
}
