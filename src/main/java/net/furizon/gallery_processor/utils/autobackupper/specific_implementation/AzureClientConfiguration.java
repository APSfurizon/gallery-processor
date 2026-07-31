package net.furizon.gallery_processor.utils.autobackupper.specific_implementation;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import com.azure.storage.blob.models.ParallelTransferOptions;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class AzureClientConfiguration {
    @NotNull
    private final AzureConfig azureConfig;

    @Bean
    @NotNull
    public BlobContainerClient getBlobClient() {
        return new BlobContainerClientBuilder()
                .endpoint(azureConfig.getEndpoint())
                .sasToken(azureConfig.getSasToken())
                .containerName(azureConfig.getContainer())
                .buildClient();
    }

    @Bean
    @NotNull
    public ParallelTransferOptions getTransferOptions() {
        return new ParallelTransferOptions()
                //block size can be null and the sdk will use the default one internally (I checked, it's 8MiB)
                .setMaxConcurrency(azureConfig.getMaxConcurrency())
                .setMaxSingleUploadSizeLong(azureConfig.getMultichunkThreshold());
    }
}
