package net.furizon.gallery_processor.utils.jobCompletedWebhook;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.validation.constraints.NotNull;
import net.furizon.gallery_processor.entity.Job;

public interface JobCompletedWebhook {
    boolean invoke(@NotNull Job job) throws JsonProcessingException;
}
