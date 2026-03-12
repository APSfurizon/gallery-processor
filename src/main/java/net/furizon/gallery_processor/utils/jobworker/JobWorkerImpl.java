package net.furizon.gallery_processor.utils.jobworker;

import com.drew.imaging.FileType;
import com.drew.imaging.FileTypeDetector;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.gallery_processor.dto.JobType;
import net.furizon.gallery_processor.dto.upload.GalleryProcessorUploadData;
import net.furizon.gallery_processor.dto.upload.UploadVideoMetadata;
import net.furizon.gallery_processor.entity.Job;
import net.furizon.gallery_processor.infrastructure.s3.actions.directDownload.S3DirectDownload;
import net.furizon.gallery_processor.infrastructure.s3.actions.directUpload.S3DirectUpload;
import net.furizon.gallery_processor.repository.JobRepository;
import net.furizon.gallery_processor.utils.extractFileType.ExtractFileType;
import net.furizon.gallery_processor.utils.extractImageMetadata.ExtractImageMetadata;
import net.furizon.gallery_processor.utils.ffmpeg.Ffmpeg;
import net.furizon.gallery_processor.utils.hashFile.HashFile;
import net.furizon.gallery_processor.utils.imagemagick.ImageMagick;
import org.apache.commons.io.FilenameUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
@Component
@RequiredArgsConstructor
public class JobWorkerImpl implements JobWorker {
    @NotNull
    private final HashFile hashFile;
    @NotNull
    private final ExtractFileType extractFileType;
    @NotNull
    private final ExtractImageMetadata extractImageMetadata;

    @NotNull
    private final S3DirectDownload s3DirectDownload;
    @NotNull
    private final S3DirectUpload s3DirectUpload;

    @NotNull
    private final ImageMagick imageMagick;
    @NotNull
    private final Ffmpeg ffmpeg;

    @NotNull
    private final JobRepository jobRepository;

    @NotNull
    private final ObjectMapper objectMapper;

    @Value("${worker.render-prefix}")
    private String renderPrefix;
    @Value("${worker.thumbnail-prefix}")
    private String thumbnailPrefix;

    @Override
    public boolean work(@NotNull Job job) {
        long jobId = job.getId();
        Path tempFile = null;
        try {
            String fullFileName = job.getName();
            String fileName = FilenameUtils.getName(fullFileName);
            String extension = FilenameUtils.getExtension(fullFileName);
            tempFile = Files.createTempFile(null, extension);
            log.info("[{}] Creating temp file {}", jobId, tempFile);

            // Download file
            log.debug("[{}] Downloading from s3 '{}'", jobId, job.getName());
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
                    log.warn("[{}] Unsupported file type {}. Marking it as UNKNOWN", jobId, fileType);
                    job.setResult("{}");
                    job.setType(JobType.UNKNOWN);
                    jobRepository.save(job);
                    return true;
                }
            }

            var dataBuilder = GalleryProcessorUploadData.builder();
            job.setType(isPhoto ? JobType.IMGAGE : JobType.VIDEO);

            //Extract filesize, hash, content type extracted already
            log.debug("[{}] Loading hash of file", jobId);
            dataBuilder.hash(hashFile.hashFile(tempFile));
            dataBuilder.mimeType(fileType.getMimeType());
            dataBuilder.fileSize(Files.size(tempFile));
            var data = dataBuilder.build();

            boolean result;
            log.debug("[{}] Executing inner function (isPhoto = {})", jobId, isPhoto);
            if (isPhoto) {
                // If image, extract exif, extract resolution
                result = handleImage(
                    job,
                    tempFile,
                    fileName,
                    data,
                    fileType,
                    needsRender
                );
            } else {
                // If video, extract first frame as (resized) thumbnail, render, video codec, audio codec, audio freq, framerate, length, timestamp, resolution
                result = handleVideo(
                        job,
                        tempFile,
                        fileName,
                        data,
                        fileType
                );
            }
            if (!result) {
                log.error("[{}] Inner function returned false!", jobId);
                return false;
            }

            job.setResult(objectMapper.writeValueAsString(data));
        } catch (IOException e) {
            log.error("IOException while working on job {}", job.getId(), e);
            return true;
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
                                @NotNull String fileName,
                                @NotNull GalleryProcessorUploadData data,
                                @NotNull FileType fileType,
                                boolean needsRender) throws IOException {
        final String tempFileName = tempFile.getFileName().toString();
        final long jobId = job.getId();
        //First round of pass, trying to get as much metadata as possible
        log.debug("[{}] First pass of exif", jobId);
        extractImageMetadata.parseExif(tempFile, data, fileType);

        if (needsRender || data.getPhotoMetadata() == null) { //if photo metadata is null we try to conver the image to a better format
            Path render = null;
            try {
                render = Files.createTempFile("rend_", tempFileName);
                log.info("[{}] Render tempfile: {}", jobId, render);
                //If needs a render do it now
                log.debug("[{}] Render", jobId);
                imageMagick.renderToWebp(tempFile, render, fileType);

                //Reload the metadata again from the rendered file (data will be merged)
                log.debug("[{}] Second pass of exif", jobId);
                extractImageMetadata.parseExif(render, data, fileType);

                //Upload rendered image to S3
                if (needsRender) {
                    log.info("[{}] Uploading render {}", jobId, render);
                    final String renderKey = renderPrefix + fileName;
                    s3DirectUpload.upload(renderKey, render);
                    data.setRenderedMediaName(renderKey);
                }
            } finally {
                if (render != null) Files.deleteIfExists(render);
            }
        }

        //If width/height are still missing, load them from the file itself
        if (data.getResolutionWidth() == 0 || data.getResolutionHeight() == 0) {
            log.debug("[{}] No resolution width or height. Retrieving with imagemagick", jobId);
            imageMagick.getWidthAndHeight(tempFile, fileType, data);
        }

        Path thumbnail = null;
        try {
            thumbnail = Files.createTempFile("thumb_", tempFileName);
            log.info("[{}] Thumbnail tempfile: {}", jobId, thumbnail);
            //Create thumbnail
            log.debug("[{}] Creating thumbnail", jobId);
            imageMagick.createThumbnail(
                    tempFile,
                    thumbnail,
                    data.getResolutionWidth(),
                    data.getResolutionHeight(),
                    fileType
            );

            //Upload thumbnail to s3
            log.info("[{}] Uploading thumbnail {}", jobId, thumbnail);
            final String thumbnailKey = thumbnailPrefix + fileName;
            s3DirectUpload.upload(thumbnailKey, thumbnail);
            data.setThumbnailMediaName(thumbnailKey);

        } finally {
            if (thumbnail != null) Files.deleteIfExists(thumbnail);
        }

        return true;
    }

    private boolean handleVideo(@NotNull Job job,
                                @NotNull Path tempFile,
                                @NotNull String fileName,
                                @NotNull GalleryProcessorUploadData data,
                                @NotNull FileType fileType) throws IOException {
        final String tempFileName = tempFile.getFileName().toString();
        final long jobId = job.getId();
        Path render = null;
        Path thumbnail = null;
        try {
            render = Files.createTempFile("rend_", tempFileName);
            thumbnail = Files.createTempFile("thumb_", tempFileName);
            log.info("[{}] Render tempfile: {}, Thumbnail tempfile: {}", jobId, render, thumbnail);

            //Get video metadata
            log.debug("[{}] Extracting metadata from video", jobId);
            ffmpeg.extractMetadata(tempFile, fileType, data);

            //Extract render frame
            log.debug("[{}] Extracting render frame from video", jobId);
            UploadVideoMetadata videoMetadata = data.getVideoMetadata();
            Integer duration = videoMetadata == null ? null : videoMetadata.getDuration();
            ffmpeg.extractFirstFrame(tempFile, render, fileType, duration == null ? 0 : duration);

            //If somehow we still don't have width and height, get it from the render
            if (data.getResolutionWidth() == 0 || data.getResolutionHeight() == 0) {
                log.debug("[{}] Trying to extract width and height from rendered frame", jobId);
                imageMagick.getWidthAndHeight(render, FileType.WebP, data);
            }

            //From the render frame extract thumbnail
            log.debug("[{}] Extracting thumbnail", jobId);
            imageMagick.createThumbnail(
                    render,
                    thumbnail,
                    data.getResolutionWidth(),
                    data.getResolutionHeight(),
                    fileType
            );

            //Upload thumbnail and render frame
            log.info("[{}] Uploading render {}", jobId, thumbnail);
            final String renderKey = renderPrefix + fileName;
            s3DirectUpload.upload(renderKey, render);
            data.setRenderedMediaName(renderKey);

            log.info("[{}] Uploading thumbnail {}", jobId, thumbnail);
            final String thumbnailKey = thumbnailPrefix + fileName;
            s3DirectUpload.upload(thumbnailKey, thumbnail);
            data.setThumbnailMediaName(thumbnailKey);

        } finally {
            if (render != null) Files.deleteIfExists(render);
            if (thumbnail != null) Files.deleteIfExists(thumbnail);
        }

        return true;
    }
}
