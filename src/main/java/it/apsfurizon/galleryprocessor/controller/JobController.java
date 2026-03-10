package it.apsfurizon.galleryprocessor.controller;

import it.apsfurizon.galleryprocessor.dto.JobRequest;
import it.apsfurizon.galleryprocessor.dto.JobResponse;
import it.apsfurizon.galleryprocessor.entity.Job;
import it.apsfurizon.galleryprocessor.service.JobAlreadyExistsException;
import it.apsfurizon.galleryprocessor.service.JobService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/jobs")
@RequiredArgsConstructor
public class JobController {

    private final JobService jobService;

    @PostMapping
    public ResponseEntity<Job> submitJob(@Valid @RequestBody JobRequest request) {
        log.debug("[JOBS] POST /jobs - submitting job with id {}", request.getId());
        Job saved = jobService.submitJob(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @ExceptionHandler(JobAlreadyExistsException.class)
    public ResponseEntity<Void> handleDuplicate(JobAlreadyExistsException ex) {
        log.warn("[JOBS] Conflict: job with id {} already exists", ex.getJobId());
        return ResponseEntity.status(HttpStatus.CONFLICT).build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<JobResponse> getJobStatus(@PathVariable Long id) {
        log.debug("[JOBS] GET /jobs/{} - querying job status", id);
        JobResponse response = jobService.getJobStatus(id);
        if (response.getStatus() == JobResponse.JobStatus.NOT_FOUND) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        return ResponseEntity.ok(response);
    }
}
