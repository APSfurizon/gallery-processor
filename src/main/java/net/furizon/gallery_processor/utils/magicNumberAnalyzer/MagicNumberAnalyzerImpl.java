package net.furizon.gallery_processor.utils.magicNumberAnalyzer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.antlr.v4.runtime.misc.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class MagicNumberAnalyzerImpl implements MagicNumberAnalyzer {

    private static final int BUFFER_SIZE = 64;

    //Supported images png, jpg, webp, heic
    //Supported videos mp4, mov, mkv, flv, avi, webm
    private static final List<Pair<Integer[], MimeType>> MIME_TYPES = List.of(
        new Pair<Integer[], MimeType>(new Integer[]{0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A}, MimeTypeUtils.IMAGE_PNG),
        new Pair<Integer[], MimeType>(new Integer[]{0xFF, 0xD8, 0xFF, 0xDB}, MimeTypeUtils.IMAGE_JPEG),
        new Pair<Integer[], MimeType>(new Integer[]{0xFF, 0xD8, 0xFF, 0xEE}, MimeTypeUtils.IMAGE_JPEG),
        new Pair<Integer[], MimeType>(new Integer[]{0xFF, 0xD8, 0xFF, 0xE1}, MimeTypeUtils.IMAGE_JPEG),
        new Pair<Integer[], MimeType>(new Integer[]{0xFF, 0xD8, 0xFF, 0xE0}, MimeTypeUtils.IMAGE_JPEG),
        new Pair<Integer[], MimeType>(new Integer[]{}, MimeTypeUtils.IMAGE_HEIC),
        new Pair<Integer[], MimeType>(new Integer[]{}, MimeTypeUtils.IMAGE_WEBP),

        new Pair<Integer[], MimeType>(new Integer[]{}, MimeTypeUtils.VIDEO_MP4),
        new Pair<Integer[], MimeType>(new Integer[]{}, MimeTypeUtils.VIDEO_MOV),
        new Pair<Integer[], MimeType>(new Integer[]{}, MimeTypeUtils.VIDEO_MKV),
        new Pair<Integer[], MimeType>(new Integer[]{}, MimeTypeUtils.VIDEO_FLV),
        new Pair<Integer[], MimeType>(new Integer[]{}, MimeTypeUtils.VIDEO_AVI),
        new Pair<Integer[], MimeType>(new Integer[]{}, MimeTypeUtils.VIDEO_WEBM),
    );

    @Override
    public @Nullable MimeType extractMimeType(@NotNull Path file, int fileLength) throws IOException {

        ByteBuffer buffer = ByteBuffer.allocate(Math.min(fileLength, BUFFER_SIZE));
        try (SeekableByteChannel channel = Files.newByteChannel(file)) {
            channel.read(buffer);
        }


        return null;
    }
}
