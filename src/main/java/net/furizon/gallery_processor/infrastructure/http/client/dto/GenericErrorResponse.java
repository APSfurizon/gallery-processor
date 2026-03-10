package net.furizon.gallery_processor.infrastructure.http.client.dto;

import lombok.Data;
import org.jetbrains.annotations.NotNull;

@Data
public class GenericErrorResponse {
    @NotNull
    private final String error;
}
