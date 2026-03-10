package it.apsfurizon.galleryprocessor.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "jobs")
public class Job {

    @Id
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "submitted_at", nullable = false)
    private Instant submittedAt;

    @Column(name = "result", columnDefinition = "text")
    private String result;

    public Job(Long id, String name, Instant submittedAt) {
        this.id = id;
        this.name = name;
        this.submittedAt = submittedAt;
    }
}
