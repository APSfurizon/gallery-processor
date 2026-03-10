package net.furizon.gallery_processor.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

@Data
public class NewJobRequest {
     @NotNull
     private Long id;
     @NotEmpty
     private String file;
}
