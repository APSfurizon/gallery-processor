package net.furizon.gallery_processor.utils.jobworker;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.gallery_processor.entity.Job;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class JobWorkerImpl implements JobWorker {
    @Override
    public void work(Job job) {

    }
}
