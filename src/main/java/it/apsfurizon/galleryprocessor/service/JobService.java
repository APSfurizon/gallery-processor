package it.apsfurizon.galleryprocessor.service;

import it.apsfurizon.galleryprocessor.dto.JobRequest;
import it.apsfurizon.galleryprocessor.dto.JobResponse;
import it.apsfurizon.galleryprocessor.entity.Job;
import it.apsfurizon.galleryprocessor.repository.JobRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Service
public class JobService {

    private final JobRepository jobRepository;

    public JobService(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    @Transactional
    public Job submitJob(JobRequest request) {
        if (jobRepository.existsById(request.getId())) {
            throw new JobAlreadyExistsException(request.getId());
        }
        Job job = new Job(request.getId(), request.getName(), Instant.now());
        return jobRepository.save(job);
    }

    @Transactional(readOnly = true)
    public JobResponse getJobStatus(Long id) {
        Optional<Job> optionalJob = jobRepository.findById(id);
        if (optionalJob.isEmpty()) {
            return new JobResponse(JobResponse.JobStatus.NOT_FOUND, null);
        }
        Job job = optionalJob.get();
        if (job.getResult() == null) {
            return new JobResponse(JobResponse.JobStatus.QUEUED, null);
        }
        return new JobResponse(JobResponse.JobStatus.DONE, job.getResult());
    }
}
