package net.furizon.gallery_processor.utils.ffmpeg.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Data
public class FfmpegStream {

    @NotNull
    @JsonProperty("codec_type")
    private final CodecType codecType;

    @Nullable
    @JsonProperty("codec_name")
    private final String codecName;

    @Nullable
    @JsonProperty("sample_rate")
    @Getter(AccessLevel.NONE)
    private final String sampleRate;

    @Nullable
    private final Integer width;
    @Nullable
    private final Integer height;

    @Nullable
    @Getter(AccessLevel.NONE)
    @JsonProperty("avg_frame_rate")
    private final String frameRate;

    @Nullable
    public String getSampleRate() {
        final String suffix = " kHz";

        if (sampleRate == null) return null;
        if (sampleRate.isEmpty()) return null;

        int length = sampleRate.length();
        if (length > 3) {
            return sampleRate.substring(0, length - 3) + "."  + sampleRate.substring(length - 3) + suffix;
        } else {
            return sampleRate + suffix;
        }
    }

    @Nullable
    public String getFrameRate() {
        final String suffix = " fps";

        if (frameRate == null) return null;
        if (frameRate.isEmpty()) return null;

        if (frameRate.contains("/")) {
            String[] sp =  frameRate.split("/");
            int a = Integer.parseInt(sp[0]);
            int b = Integer.parseInt(sp[1]);
            if (b == 0) return "0" + suffix;
            return String.format("%.2f", ((float) a / (float) b)) + suffix;
        } else {
            return frameRate + suffix;
        }
    }
}
