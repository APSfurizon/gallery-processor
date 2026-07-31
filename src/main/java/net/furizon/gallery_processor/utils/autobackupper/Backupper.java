package net.furizon.gallery_processor.utils.autobackupper;

import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

public interface Backupper {
    void uploadFile(@NotNull String key, @NotNull Path file);
}
