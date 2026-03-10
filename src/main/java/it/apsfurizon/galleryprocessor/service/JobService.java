package it.apsfurizon.galleryprocessor.service;

import it.apsfurizon.galleryprocessor.dto.JobRequest;
import it.apsfurizon.galleryprocessor.dto.JobResponse;
import it.apsfurizon.galleryprocessor.entity.Job;
import it.apsfurizon.galleryprocessor.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobService {

    private final JobRepository jobRepository;

    @Transactional
    public Job submitJob(JobRequest request) {
        log.info("[JOBS] Submitting job with id {} and name '{}'", request.getId(), request.getName());
        if (jobRepository.existsById(request.getId())) {
            log.warn("[JOBS] Job with id {} already exists", request.getId());
            throw new JobAlreadyExistsException(request.getId());
        }
        Job job = new Job(request.getId(), request.getName(), Instant.now());
        Job saved = jobRepository.save(job);
        log.info("[JOBS] Job {} submitted successfully", saved.getId());
        return saved;
    }

    @Transactional(readOnly = true)
    public JobResponse getJobStatus(Long id) {
        log.debug("[JOBS] Querying status for job id {}", id);
        Optional<Job> optionalJob = jobRepository.findById(id);
        if (optionalJob.isEmpty()) {
            log.debug("[JOBS] Job {} not found", id);
            return new JobResponse(JobResponse.JobStatus.NOT_FOUND, null);
        }
        Job job = optionalJob.get();
        if (job.getResult() == null) {
            log.debug("[JOBS] Job {} is queued (no result yet)", id);
            return new JobResponse(JobResponse.JobStatus.QUEUED, null);
        }
        log.debug("[JOBS] Job {} is done", id);
        return new JobResponse(JobResponse.JobStatus.DONE, job.getResult());
    }
}
