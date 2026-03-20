package net.furizon.gallery_processor.dto;

import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

@Data
public class SupportedTypeResponse {
    @NotNull private final Set<String> supportedFileExtensions;
    @NotNull private final Set<String> supportedMimeTypes;
}
