package net.furizon.gallery_processor.infrastructure.config.security;

import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties("security")
public class SecurityConfig {

    @NotNull
    private final String httpUsername;

    @NotNull
    private final String httpPassword;
}
