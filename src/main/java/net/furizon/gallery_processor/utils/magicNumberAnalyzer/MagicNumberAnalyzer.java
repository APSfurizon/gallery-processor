package net.furizon.gallery_processor.utils.magicNumberAnalyzer;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.util.MimeType;

import java.io.IOException;
import java.nio.file.Path;

public interface MagicNumberAnalyzer {
    @Nullable
    MimeType extractMimeType(@NotNull Path file, int fileLength) throws IOException;
}
