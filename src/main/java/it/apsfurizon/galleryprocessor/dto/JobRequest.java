package it.apsfurizon.galleryprocessor.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class JobRequest {

    @NotNull
    private final Long id;

    @NotBlank
    private final String name;
}
