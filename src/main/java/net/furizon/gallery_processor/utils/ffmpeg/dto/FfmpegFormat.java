package net.furizon.gallery_processor.utils.ffmpeg.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

@Data
public class FfmpegFormat {
    @Nullable
    @Getter(AccessLevel.NONE)
    @JsonProperty("start_time")
    private final String startTime;

    @Nullable
    @Getter(AccessLevel.NONE)
    private final String duration;

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
}
