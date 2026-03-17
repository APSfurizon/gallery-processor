package net.furizon.gallery_processor.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import net.furizon.gallery_processor.dto.upload.GalleryProcessorUploadData;
import net.furizon.gallery_processor.entity.Job;
import org.jetbrains.annotations.Nullable;

@Data
@Builder
public class JobResponse {
    private long id;
    @Nullable private String file;
    @NotNull private JobStatus status;
    @Nullable private JobType type;
    @Nullable private GalleryProcessorUploadData result;


    @NotNull
    public static JobResponse notFound(long id) {
        return new JobResponse(id, null, JobStatus.NOT_FOUND, null, null);
    }

    @NotNull
    public static JobResponse map(@NotNull Job job,
                                  int jobMaxRetries,
                                  @NotNull ObjectMapper objectMapper) throws JsonProcessingException {
        var jobResult = job.getResult();
        JobType jobType = job.getType();
        JobStatus status = jobResult == null ? JobStatus.PENDING : JobStatus.DONE;
        //if (jobType == JobType.UNKNOWN) status = JobStatus.FAILED;
        if (job.getRetries() >= jobMaxRetries) status = JobStatus.FAILED;

        return JobResponse.builder()
                .id(job.getId())
                .file(job.getName())
                .status(status)
                .type(job.getType())
                .result(jobResult == null || jobType == JobType.UNKNOWN ? null : objectMapper.readValue(jobResult, GalleryProcessorUploadData.class))
                .build();
    }
}
