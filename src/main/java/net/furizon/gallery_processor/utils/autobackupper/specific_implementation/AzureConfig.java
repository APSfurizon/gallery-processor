package net.furizon.gallery_processor.utils.autobackupper.specific_implementation;

import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "backupper.azure")
public class AzureConfig {
    private final boolean enabled;

    @NotNull private final String endpoint;
    @NotNull private final String sasToken;
    @NotNull private final String container;

    private final long multichunkThreshold;
    private final int maxConcurrency;
}
