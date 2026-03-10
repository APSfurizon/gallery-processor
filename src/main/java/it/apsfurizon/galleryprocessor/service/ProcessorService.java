package it.apsfurizon.galleryprocessor.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class ProcessorService {

    private static final Logger log = LoggerFactory.getLogger(ProcessorService.class);

    @Scheduled(fixedRateString = "${app.scheduler.rate-ms}")
    public void process() {
        // TODO: fill with processing logic
        log.debug("ProcessorService.process() triggered");
    }
}
