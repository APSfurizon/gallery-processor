package net.furizon.gallery_processor.controller;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.gallery_processor.dto.JobResponse;
import net.furizon.gallery_processor.dto.JobStatus;
import net.furizon.gallery_processor.dto.NewJobRequest;
import net.furizon.gallery_processor.dto.upload.GalleryProcessorUploadData;
import net.furizon.gallery_processor.entity.Job;
import net.furizon.gallery_processor.repository.JobRepository;
import net.furizon.gallery_processor.service.WorkerManagementService;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/")
    //@InternalAuthorize
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

    @GetMapping("/{jobId}")
    //@InternalAuthorize
    public JobResponse retrieve(@NotNull @PathVariable @Positive @Valid Long jobId, HttpServletResponse response) throws JacksonException {
        Optional<Job> res = jobRepository.findById(jobId);
        if (res.isPresent()) {
            log.info("Retrieving job {} from the queue", jobId);
            Job job = res.get();
            return JobResponse.builder()
                    .id(jobId)
                    .file(job.getName())
                    .status(job.getResult() == null ? JobStatus.PENDING : JobStatus.DONE)
                    .type(job.getType())
                    .result(job.getResult() == null ? null : objectMapper.readValue(job.getResult(), GalleryProcessorUploadData.class))
                .build();
        }
        log.info("Job {} not found in queue", jobId);
        //response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        return JobResponse.notFound(jobId);
    }
}
