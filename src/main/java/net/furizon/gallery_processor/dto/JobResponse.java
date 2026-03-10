package net.furizon.gallery_processor.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import org.jetbrains.annotations.Nullable;

@Data
@Builder
public class JobResponse {
    @NotNull private Long id;
    @Nullable private String file;
    @NotNull private JobStatus status;
    @Nullable private JobType type;
    @Nullable private Object result;

    public static JobResponse notFound(long id) {
        return new JobResponse(id, null, JobStatus.NOT_FOUND, null, null);
    }
}
