package net.furizon.gallery_processor.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.gallery_processor.entity.Job;
import net.furizon.gallery_processor.repository.JobRepository;
import net.furizon.gallery_processor.utils.jobworker.JobWorker;
import org.jetbrains.annotations.NotNull;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkerManagementService {
    @NotNull
    private final JobRepository jobRepository;

    @NotNull
    private final JobWorker jobWorker;

    private final AtomicReference<Boolean> working = new AtomicReference<>(false);

    @Scheduled(fixedRateString = "${worker.delay}", timeUnit = TimeUnit.SECONDS)
    public void run(){
        try {
            boolean isWorking = working.getAndSet(true);
            if(isWorking){
                log.debug("Already working on some requests");
                return;
            }

            List<Job> jobs;
            do {
                jobs = jobRepository.findAllByResultIsNullOrderBySubmittedAtAsc();
                jobs.forEach(job -> {
                    try {
                        jobWorker.work(job);
                    } catch (Exception e) {
                        log.error("Exception {} occurred while working on job {}", e.getMessage(), job.getId(), e);
                    }
                });
            } while (!jobs.isEmpty());
        } finally {
            working.set(false);
        }
    }
}
