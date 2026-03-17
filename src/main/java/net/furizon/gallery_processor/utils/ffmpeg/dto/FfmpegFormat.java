package net.furizon.gallery_processor.utils.ffmpeg.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.time.OffsetDateTime;

@Data
public class FfmpegFormat {
    @Nullable
    @Getter(AccessLevel.NONE)
    @JsonProperty("start_time")
    private final String startTime;

    @Nullable
    @Getter(AccessLevel.NONE)
    private final String duration;

    @Nullable
    private final Tags tags;

    public @Nullable Float getStartTime() {
        if (startTime == null) return null;
        if  (startTime.isEmpty()) return null;
        return Float.parseFloat(startTime);
    }

    public @Nullable Float getDuration() {
        if (duration == null) return null;
        if  (duration.isEmpty()) return null;

        if (startTime != null && !startTime.isEmpty()) return Float.parseFloat(duration) - Float.parseFloat(startTime);
        return Float.parseFloat(duration);
    }

    public @Nullable Integer getDurationMs() {
        Float duration = getDuration();
        if (duration == null) return null;
        return (int)(duration * 1000.0);
    }

    @Data
    public static class Tags {
        @Nullable
        @Getter(AccessLevel.NONE)
        @JsonProperty("creation_time")
        private final String creationTime;

        public @Nullable OffsetDateTime getCreationTime() {
            if (creationTime == null) return null;
            if  (creationTime.isEmpty()) return null;
            return OffsetDateTime.parse(creationTime);
        }
    }
}
