package net.furizon.gallery_processor.utils.autobackupper.specific_implementation;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.models.ParallelTransferOptions;
import com.azure.storage.blob.options.BlobUploadFromFileOptions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.gallery_processor.utils.autobackupper.Backupper;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.nio.file.Path;

@Slf4j
@Service
@RequiredArgsConstructor
class AzureBackupperService implements Backupper {
    //We use azure free credits to store in cold storage a backup of our gallery.
    //If you want to use any other service, you may want to rewrite all classes in this package

    @NotNull
    private final AzureConfig azureConfig;

    @NotNull
    private final BlobContainerClient blobClient;

    @NotNull
    private final ParallelTransferOptions parallelOptions;


    @Override
    public void uploadFile(@NotNull String key, @NotNull Path file) {
        if (!azureConfig.isEnabled()) {
            return;
        }

        try {
            log.debug("Uploading file {} to Azure backup", file);
            BlobClient client = blobClient.getBlobClient(key);

            BlobUploadFromFileOptions uploadOptions = new BlobUploadFromFileOptions(file.toAbsolutePath().toString())
                    .setParallelTransferOptions(parallelOptions);

            client.uploadFromFileWithResponse(uploadOptions, null, null);

            log.info("File {} uploaded to Azure backup to key {}", file, key);
        } catch (Exception e) {
            log.error("Error uploading file {} (key {}) to Azure backup:", file, key, e);
        }
    }
}
