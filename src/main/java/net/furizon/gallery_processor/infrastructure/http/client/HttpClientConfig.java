package net.furizon.gallery_processor.infrastructure.http.client;

import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;


@Data
@ConfigurationProperties(prefix = "http-client")
public class HttpClientConfig implements HttpConfig {
    @Override
    public @NotNull String getBaseUrl() {
        return "";
    }
}
