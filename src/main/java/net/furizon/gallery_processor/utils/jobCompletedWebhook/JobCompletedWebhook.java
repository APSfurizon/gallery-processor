package net.furizon.gallery_processor.utils.jobCompletedWebhook;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.validation.constraints.NotNull;
import net.furizon.gallery_processor.entity.Job;
import org.springframework.scheduling.annotation.Async;

public interface JobCompletedWebhook {
    boolean invoke(@NotNull Job job) throws JsonProcessingException;

    @Async
    void runAsync(@NotNull Job job, long jobId);
}
