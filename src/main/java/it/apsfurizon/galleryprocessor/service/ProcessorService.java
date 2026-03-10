package it.apsfurizon.galleryprocessor.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ProcessorService {

    @Scheduled(fixedRateString = "${app.scheduler.rate-ms}")
    public void process() {
        // TODO: fill with processing logic
        log.debug("[PROCESSOR] Process triggered");
    }
}
