package it.apsfurizon.galleryprocessor.service;

import lombok.Getter;

@Getter
public class JobAlreadyExistsException extends RuntimeException {

    private final Long jobId;

    public JobAlreadyExistsException(Long jobId) {
        super("Job with id " + jobId + " already exists");
        this.jobId = jobId;
    }
}
