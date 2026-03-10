package net.furizon.gallery_processor.repository;

import net.furizon.gallery_processor.entity.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JobRepository extends CrudRepository<Job, Long> {
    Job findById(long id);
}