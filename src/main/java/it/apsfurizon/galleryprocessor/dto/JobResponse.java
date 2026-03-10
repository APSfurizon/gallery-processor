package it.apsfurizon.galleryprocessor.dto;

public class JobResponse {

    private JobStatus status;
    private String result;

    public JobResponse(JobStatus status, String result) {
        this.status = status;
        this.result = result;
    }

    public JobStatus getStatus() {
        return status;
    }

    public void setStatus(JobStatus status) {
        this.status = status;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public enum JobStatus {
        NOT_FOUND,
        QUEUED,
        DONE
    }
}
