package net.furizon.gallery_processor.repository;

import net.furizon.gallery_processor.entity.Job;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JobRepository extends CrudRepository<Job, Long> {
    Optional<Job> findFirstByResultIsNullOrderBySubmittedAtAsc();
    List<Job> findAllByResultIsNullAndRetriesLessThanOrderBySubmittedAtAsc(int retries);
}