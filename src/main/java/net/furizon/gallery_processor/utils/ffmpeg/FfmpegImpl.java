package net.furizon.gallery_processor.utils.ffmpeg;

import com.drew.imaging.FileType;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.gallery_processor.dto.upload.GalleryProcessorUploadData;
import net.furizon.gallery_processor.dto.upload.UploadVideoMetadata;
import net.furizon.gallery_processor.utils.cmdExecutor.CmdExecutor;
import net.furizon.gallery_processor.utils.ffmpeg.dto.FfmpegFormat;
import net.furizon.gallery_processor.utils.ffmpeg.dto.FfmpegOutput;
import net.furizon.gallery_processor.utils.ffmpeg.dto.FfmpegStream;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class FfmpegImpl implements Ffmpeg {
    @NotNull
    private final CmdExecutor cmdExecutor;

    @NotNull
    private final ObjectMapper objectMapper;

    @Value("${cmd-executor.ffmpeg-binary}")
    private String ffmpegBinary;
    @Value("${cmd-executor.ffprobe-binary}")
    private String ffprobeBinary;

    @Value("${worker.thumbnail.video-max-seek}")
    private int videoMaxSeek;
    @Value("${worker.quality}")
    private Integer quality;

    @Override
    public void extractMetadata(@NotNull Path file, @NotNull FileType fileType, @NotNull GalleryProcessorUploadData data) throws IOException {
        log.info("Extracting metadata from file {}", file);
        String result = cmdExecutor.execute(
            "FFMPEG_EXTRACTMETADATA",
                ffprobeBinary,
                "-v", "error",
                "-show_entries", "format_tags=creation_time:format=duration,start_time:stream=codec_name,codec_type,sample_rate,avg_frame_rate,width,height",
                "-of", "json",
                file.toAbsolutePath().toString()
        );
        if (result == null || result.isEmpty()) {
            log.error("Unable to extract metadata from file {}: Command execution was empty", file);
            throw new IOException("");
        }
        FfmpegOutput output = objectMapper.readValue(result, FfmpegOutput.class);
        String audioFrequency = null;
        String videoCodec = null;
        String audioCodec = null;
        String frameRate = null;
        Integer width = null;
        Integer height = null;
        Integer duration = null;
        OffsetDateTime shotTs = null;

        FfmpegFormat format = output.getFormat();
        if (format != null) {
            duration = format.getDurationMs();
            FfmpegFormat.Tags tags = format.getTags();
            if (tags != null) {
                shotTs = tags.getCreationTime();
            }
        }

        List<FfmpegStream> streams = output.getStreams();
        if (streams != null) {
            for (FfmpegStream stream : streams) {
                String codecType = stream.getCodecType();
                if (codecType.equalsIgnoreCase("audio")) {
                    audioCodec = stream.getCodecName();
                    audioFrequency = stream.getSampleRate();
                }
                if (codecType.equalsIgnoreCase("video")) {
                    videoCodec = stream.getCodecName();
                    width = stream.getWidth();
                    height = stream.getHeight();
                    frameRate = stream.getFrameRate();
                }
            }
        }

        if (data.getResolutionWidth() == 0 && width != null) data.setResolutionWidth(width);
        if (data.getResolutionHeight() == 0 && height != null) data.setResolutionHeight(height);
        if (data.getShotTimestamp() == null) data.setShotTimestamp(shotTs);

        if (audioFrequency == null
                && videoCodec == null
                && audioCodec == null
                && frameRate == null
                && duration == null) {
            log.warn("Early return from {} since all params were null", file);
            return;
        }

        if (data.getVideoMetadata() == null) {
            var metadata = UploadVideoMetadata.builder()
                    .audioFrequency(audioFrequency)
                    .videoCodec(videoCodec)
                    .audioCodec(audioCodec)
                    .framerate(frameRate)
                    .duration(duration == null ? 0 : duration)
                .build();
            data.setVideoMetadata(metadata);
        } else {
            UploadVideoMetadata videoData = data.getVideoMetadata();
            if (videoData.getAudioFrequency() == null) videoData.setAudioFrequency(audioCodec);
            if (videoData.getVideoCodec() == null) videoData.setVideoCodec(videoCodec);
            if (videoData.getAudioCodec() == null) videoData.setAudioCodec(audioCodec);
            if (videoData.getFramerate() == null) videoData.setFramerate(frameRate);
            if (videoData.getDuration() == 0 && duration != null) videoData.setDuration(duration);
        }
    }

    @Override
    public void extractFirstFrame(@NotNull Path source, @NotNull Path dest, @NotNull FileType fileType, int duration) throws IOException {
        log.info("Extracting first frame from file {} to file {}", source, dest);
        int seek = Math.min(duration / 1000, videoMaxSeek) % 60;
        cmdExecutor.execute(
            "FFMPEG_FIRSTFRAME",
                ffmpegBinary,
                "-y",
                "-v", "error",
                "-ss", String.format("00:00:%02d", seek),
                "-i", source.toAbsolutePath().toString(),
                "-vframes", "1",
                "-vcodec", "libwebp",
                "-lossless", "0",
                "-compression_level", "6",
                "-q:v", quality.toString(),
                "-pix_fmt", "yuv420p",
                dest.toAbsolutePath().toString()
        );
    }
}
