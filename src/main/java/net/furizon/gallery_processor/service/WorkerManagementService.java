package net.furizon.gallery_processor.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.gallery_processor.entity.Job;
import net.furizon.gallery_processor.repository.JobRepository;
import net.furizon.gallery_processor.utils.jobCompletedWebhook.JobCompletedWebhook;
import net.furizon.gallery_processor.utils.jobworker.JobWorker;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
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

    @NotNull
    private final JobCompletedWebhook jobCompletedWebhook;

    @Value("${worker.max-retries}")
    private int maxRetries;

    private final AtomicReference<Boolean> working = new AtomicReference<>(false);

    @Scheduled(fixedRateString = "${worker.delay}", timeUnit = TimeUnit.SECONDS)
    public void run(){
        try {
            boolean isWorking = working.getAndSet(true);
            if(isWorking){
                log.debug("Already working on some requests");
                return;
            }

            Set<Long> invocationProcessedJobs = new HashSet<>();
            boolean foundNewJob;
            List<Job> jobs;
            do {
                foundNewJob = false;
                jobs = jobRepository.findAllByResultIsNullAndRetriesLessThanOrderBySubmittedAtAsc(maxRetries);
                for(@NotNull Job job : jobs) {
                    long jobId = job.getId();
                    try {
                        //Skip jobs we already processed in this invocation
                        if (invocationProcessedJobs.add(jobId)) {
                            foundNewJob = true;

                            //Proper processing
                            boolean result = jobWorker.work(job);
                            if (result) {
                                log.info("Job {} has been successfully executed", jobId);
                                try {
                                    jobCompletedWebhook.invoke(job);
                                } catch (Exception e) {
                                    log.warn("Exception while calling webhook for job {}", jobId, e);
                                }
                            } else {
                                log.warn("Job {} failed. Will not retry...", jobId);
                                job.setRetries(Integer.MAX_VALUE);
                                jobRepository.save(job);
                            }
                        } else log.info("Job {} has already been processed in this invocation. Skipping", jobId);
                    } catch (Exception e) {
                        log.error("Exception {} occurred while working on job {}", e.getMessage(), jobId, e);
                        //Update retries
                        jobRepository.save(job.incRetries());
                    }
                }
            } while (!jobs.isEmpty() && foundNewJob);
        } finally {
            working.set(false);
        }
    }

    @Async
    public void runAsync() {
        run();
    }
}
