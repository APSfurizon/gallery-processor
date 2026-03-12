package net.furizon.gallery_processor.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import net.furizon.gallery_processor.dto.upload.GalleryProcessorUploadData;
import org.jetbrains.annotations.Nullable;

@Data
@Builder
public class JobResponse {
    private long id;
    @Nullable private String file;
    @NotNull private JobStatus status;
    @Nullable private JobType type;
    @Nullable private GalleryProcessorUploadData result;


    public static JobResponse notFound(long id) {
        return new JobResponse(id, null, JobStatus.NOT_FOUND, null, null);
    }
}
