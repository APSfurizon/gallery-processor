package net.furizon.gallery_processor.utils.jobCompletedWebhook;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.gallery_processor.dto.JobResponse;
import net.furizon.gallery_processor.entity.Job;
import net.furizon.gallery_processor.infrastructure.http.client.HttpClient;
import net.furizon.gallery_processor.infrastructure.http.client.HttpRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class JobCompletedWebhookImpl {
    @NotNull
    private final HttpClient httpClient;

    @NotNull
    private final ObjectMapper objectMapper;

    @Value("${worker.max-retries}")
    private int maxRetries;
    @Value("${worker.job-completed-webhook.url}")
    private String endpoint;

    public void invoke(@NotNull Job job) throws JsonProcessingException {
        HttpRequest.<Boolean>create()
            .method(HttpMethod.POST)
            .path("")
            .overrideBaseUrl(endpoint)
            .body(JobResponse.map(job, maxRetries, objectMapper))
            .responseType(Boolean.class)
            .build();
    }
}
