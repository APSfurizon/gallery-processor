package net.furizon.gallery_processor.entity;


import jakarta.annotation.Nullable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.furizon.gallery_processor.dto.JobType;
import net.furizon.gallery_processor.dto.NewJobRequest;

@Data
@Entity
@NoArgsConstructor
@Table(name = "jobs")
public class Job {

    @Id
    @Positive
    private long id;

    @NotEmpty
    private String name;

    @Column(name = "submitted_at")
    @Positive
    private long submittedAt;

    @Nullable
    @Column(columnDefinition = "TEXT")
    private String result;

    @Column(columnDefinition = "integer default 0")
    @PositiveOrZero
    private int retries = 0;

    @Nullable
    private JobType type;

    public Job incRetries() {
        this.retries++;
        return this;
    }

    public Job(Long id, String name, long submittedAt) {
        this.id = id;
        this.name = name;
        this.submittedAt = submittedAt;
        this.result = null;
    }

    public Job(NewJobRequest newJobRequest) {
        this.id = newJobRequest.getId();
        this.name = newJobRequest.getFile();
        this.submittedAt = System.currentTimeMillis();
        this.result = null;
        this.type = null;
    }
}