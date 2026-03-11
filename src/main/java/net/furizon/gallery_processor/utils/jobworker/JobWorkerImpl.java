package net.furizon.gallery_processor.utils.jobworker;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.gallery_processor.entity.Job;
import net.furizon.gallery_processor.infrastructure.s3.actions.directDownload.S3DirectDownload;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
@Component
@RequiredArgsConstructor
public class JobWorkerImpl implements JobWorker {
    @NotNull
    private final S3DirectDownload s3DirectDownload;


    @Override
    public boolean work(@NotNull Job job) {
        Path tempFile = null;
        try {
            tempFile = Files.createTempFile(null, job.getName());

            // Download file
            s3DirectDownload.toFile(job.getName(), tempFile);

            // Check magicnumbers for mimetype. If unsupported, quit by setting his type to unknown and empty result field. File deletion will be handled by backend
            int fileSize = (int) Files.size(tempFile);
            String mimeType = Files.probeContentType(tempFile);
            //TODO

            //Extract filesize, hash, content type extracted already

            // If video, extract first frame as (resized) thumbnail, video codec, audio codec, audio freq, framerate, length, timestamp, resolution
            // If image, extract exif, extract resolution
        } catch (IOException e) {
            log.error("IOException while working on job {}", job.getId(), e);
            return false;
        } catch (NoSuchKeyException e) {
            log.error("NoSuchKey while working on job {}", job.getId(), e);
            return false;
        } finally {
            if (tempFile != null) {
                try {
                    Files.deleteIfExists(tempFile);
                } catch (IOException e) {
                    log.error("IOException while deleting temp file {} for job {}", tempFile, job.getId(), e);
                }
            }
        }
        return true;
    }

    @PostConstruct
    public void init() {
        work(new Job());
    }
}
