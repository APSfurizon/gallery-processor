package net.furizon.gallery_processor.utils.jobworker;

import net.furizon.gallery_processor.entity.Job;

public interface JobWorker {
    boolean work(Job job);
}
