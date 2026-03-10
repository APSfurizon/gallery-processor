package it.apsfurizon.galleryprocessor.dto;

import lombok.Data;

@Data
public class JobResponse {

    private final JobStatus status;
    private final String result;

    public enum JobStatus {
        NOT_FOUND,
        QUEUED,
        DONE
    }
}
