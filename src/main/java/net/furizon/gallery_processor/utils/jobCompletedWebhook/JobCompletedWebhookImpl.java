package net.furizon.gallery_processor.utils.jobCompletedWebhook;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.gallery_processor.dto.JobResponse;
import net.furizon.gallery_processor.entity.Job;
import net.furizon.gallery_processor.infrastructure.http.client.DefaultHttpClientConfig;
import net.furizon.gallery_processor.infrastructure.http.client.HttpClient;
import net.furizon.gallery_processor.infrastructure.http.client.HttpRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class JobCompletedWebhookImpl implements JobCompletedWebhook {
    @NotNull
    private final HttpClient httpClient;

    @NotNull
    private final ObjectMapper objectMapper;

    @Value("${worker.max-retries}")
    private int maxRetries;
    @Value("${worker.job-completed-webhook.url}")
    private String endpoint;
    @Value("${worker.job-completed-webhook.user}")
    private String user;
    @Value("${worker.job-completed-webhook.password}")
    private String password;

    @Override
    public boolean invoke(@NotNull Job job) throws JsonProcessingException {
        if (endpoint == null || endpoint.isEmpty()) {
            return true;
        }
        var req = HttpRequest.<Boolean>create()
            .method(HttpMethod.POST)
            .path("")
            .overrideBaseUrl(endpoint)
            .contentType(MediaType.APPLICATION_JSON)
            .body(JobResponse.map(job, maxRetries, objectMapper))
            .responseType(Boolean.class)
            .basicAuth(user, password)
            .build();

        try {
            return Optional
                    .ofNullable(httpClient.send(DefaultHttpClientConfig.class, req).getBody())
                    .orElse(false);
        } catch (final HttpClientErrorException ex) {
            log.error(ex.getResponseBodyAsString());
            return false;
        }
    }
}
