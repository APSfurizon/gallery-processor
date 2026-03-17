package net.furizon.gallery_processor.controller;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.gallery_processor.dto.JobResponse;
import net.furizon.gallery_processor.dto.JobStatus;
import net.furizon.gallery_processor.dto.NewJobRequest;
import net.furizon.gallery_processor.entity.Job;
import net.furizon.gallery_processor.infrastructure.security.InternalAuthorize;
import net.furizon.gallery_processor.repository.JobRepository;
import net.furizon.gallery_processor.service.WorkerManagementService;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/job")
@RequiredArgsConstructor
public class JobController {
    @NotNull
    private final JobRepository jobRepository;

    @NotNull
    private final WorkerManagementService workerService;

    @NotNull
    private final ObjectMapper objectMapper;

    @Value("${worker.max-retries}")
    private int jobMaxRetries;

    @Operation(summary = "Submits a job", description =
        "The job request is made by an id which should identify the file in the original database "
        + "together with the filename which is the key to download the file from the aws bucket. "
        + "The returned object is a JobResponse with the original id and filename set, together with "
        + "the job status: If a job with the same id is existed, it is specified here")
    @PostMapping("/")
    @InternalAuthorize
    public JobResponse store(@Valid @NotNull @RequestBody NewJobRequest newJobRequest) {
        boolean alreadyExists = jobRepository.existsById(newJobRequest.getId());
        if (!alreadyExists) {
            log.info("Storing job {} in the queue", newJobRequest.getId());
            jobRepository.save(new Job(newJobRequest));
            workerService.runAsync();
        } else {
            log.info("Received job request for {} but it already exists", newJobRequest.getId());
        }
        return JobResponse.builder()
                .id(newJobRequest.getId())
                .file(newJobRequest.getFile())
                .status(alreadyExists ? JobStatus.ALREADY_EXISTS : JobStatus.PENDING)
            .build();
    }

    @Operation(summary = "Retrieves the status of a job", description =
        "Keep in my that we are (sorry) always returning 200. The returning object is composed by the original id and fileName. "
        + "The job status can be PENDING if we still have to work on it, or DONE if we're completed successfully. "
        + "A FAILED state means either the media type is unknown (you can check the type field) and you may want to delete it from your db as well, "
        + "or there were errors happening and we've hit the max retries. In this instance, you can POST `/{jobId}/retry` to reset its chances. "
        + "In the type field you can also find if we identified the upload as video or image. You can get the results of our "
        + "analysis in the result field, which have two optionals objects: photoMetadata and videoMetadata. They might be populated "
        + "based on the type field, but if we weren't able to extract metadata from the image they still be null.")
    @GetMapping("/{jobId}")
    @InternalAuthorize
    public JobResponse retrieve(@NotNull @PathVariable @Positive @Valid Long jobId, HttpServletResponse response) throws JacksonException {
        Optional<Job> res = jobRepository.findById(jobId);
        if (res.isPresent()) {
            log.info("Retrieving job {} from the queue", jobId);
            return JobResponse.map(
                    res.get(),
                    jobMaxRetries,
                    objectMapper
            );
        }
        log.info("Job {} not found in queue", jobId);
        //response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        return JobResponse.notFound(jobId);
    }

    @Operation(summary = "Give the job another chance :)", description =
        "This method effectively resets the retry number, so it can be processed again")
    @PostMapping("/{jobId}/retry")
    @InternalAuthorize
    public JobResponse retry(@NotNull @PathVariable @Positive @Valid Long jobId, HttpServletResponse response) {
        Optional<Job> res = jobRepository.findById(jobId);
        if (res.isPresent()) {
            log.info("Retrying job {}", jobId);
            Job job = res.get();
            job.setRetries(0);
            jobRepository.save(job);
            workerService.runAsync();

            return JobResponse.builder()
                    .id(jobId)
                    .file(job.getName())
                    .status(JobStatus.PENDING)
                    .type(job.getType())
                    .result(null)
                .build();
        }
        log.info("Job {} not found", jobId);
        //response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        return JobResponse.notFound(jobId);
    }
}
