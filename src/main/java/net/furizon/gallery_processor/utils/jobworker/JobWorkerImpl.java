package net.furizon.gallery_processor.utils.jobworker;

import com.drew.imaging.FileType;
import com.drew.imaging.FileTypeDetector;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.gallery_processor.dto.JobType;
import net.furizon.gallery_processor.dto.upload.GalleryProcessorUploadData;
import net.furizon.gallery_processor.entity.Job;
import net.furizon.gallery_processor.infrastructure.s3.actions.directDownload.S3DirectDownload;
import net.furizon.gallery_processor.repository.JobRepository;
import net.furizon.gallery_processor.utils.extractFileType.ExtractFileType;
import net.furizon.gallery_processor.utils.hashFile.HashFile;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JobWorkerImpl implements JobWorker {
    @NotNull
    private final S3DirectDownload s3DirectDownload;
    @NotNull
    private final ExtractFileType extractFileType;
    @NotNull
    private final HashFile hashFile;

    @NotNull
    private final JobRepository jobRepository;

    @NotNull
    private final ObjectMapper objectMapper;

    @Override
    public boolean work(@NotNull Job job) {
        Path tempFile = null;
        try {
            tempFile = Files.createTempFile(null, job.getName());

            // Download file
            s3DirectDownload.toFile(job.getName(), tempFile);

            // Check magicnumbers for mimetype. If unsupported, quit by setting his type to unknown and empty result field. File deletion will be handled by backend
            FileType fileType = extractFileType.invoke(tempFile);
            boolean isPhoto;
            boolean needsRender;
            // https://github.com/drewnoakes/metadata-extractor/blob/main/Source/com/drew/imaging/FileType.java
            // https://imagemagick.org/script/formats.php
            switch (fileType) {
                case FileType.Png:
                case FileType.Jpeg:
                case FileType.WebP:
                    isPhoto = true;
                    needsRender = false;
                    break;
                case FileType.Heif:
                case FileType.Tiff:
                case FileType.Arw:
                case FileType.Cr2:
                case FileType.Crw:
                case FileType.Nef:
                case FileType.Orf:
                //case FileType.Pef: //TODO watch for updates
                case FileType.Raf:
                    isPhoto = true;
                    needsRender = true;
                    break;
                case FileType.Flv:
                case FileType.Avi:
                case FileType.QuickTime: //mov
                case FileType.Mp4:
                    isPhoto = false;
                    needsRender = true;
                    break;
                default: {
                    log.warn("Unsupported file type {} for job {}. Marking it as UNKNOWN", fileType,  job.getId());
                    job.setResult("{}");
                    job.setType(JobType.UNKNOWN);
                    jobRepository.save(job);
                    return true;
                }
            }

            var dataBuilder = GalleryProcessorUploadData.builder();
            job.setType(isPhoto ? JobType.IMGAGE : JobType.VIDEO);

            //Extract filesize, hash, content type extracted already
            dataBuilder.fileSize(Files.size(tempFile));
            dataBuilder.hash(hashFile.hashFile(tempFile));
            dataBuilder.mimeType(fileType.getMimeType());

            // If video, extract first frame as (resized) thumbnail, video codec, audio codec, audio freq, framerate, length, timestamp, resolution
            // If image, extract exif, extract resolution

            job.setResult(objectMapper.writeValueAsString(dataBuilder.build()));
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

    private boolean handleImage(@NotNull Job job,
                                @NotNull Path tempFile,
                                @NotNull GalleryProcessorUploadData.GalleryProcessorUploadDataBuilder builder,
                                @NotNull FileType fileType,
                                boolean needsRender) {

    }

    private boolean handleVideo(@NotNull Job job,
                                @NotNull Path tempFile,
                                @NotNull GalleryProcessorUploadData.GalleryProcessorUploadDataBuilder builder,
                                @NotNull FileType fileType) {

    }
}
