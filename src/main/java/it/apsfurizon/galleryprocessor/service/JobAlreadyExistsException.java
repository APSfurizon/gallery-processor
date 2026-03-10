package it.apsfurizon.galleryprocessor.service;

public class JobAlreadyExistsException extends RuntimeException {

    private final Long jobId;

    public JobAlreadyExistsException(Long jobId) {
        super("Job with id " + jobId + " already exists");
        this.jobId = jobId;
    }

    public Long getJobId() {
        return jobId;
    }
}
