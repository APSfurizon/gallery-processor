package net.furizon.gallery_processor.infrastructure.http.client.dto;

import lombok.Data;
import org.jetbrains.annotations.NotNull;

@Data
public class GenericMessageResponse {
    @NotNull
    private final String message;
}
