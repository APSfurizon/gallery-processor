package net.furizon.gallery_processor.utils.extractFileType;

import com.drew.imaging.FileType;
import com.drew.imaging.FileTypeDetector;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
@RequiredArgsConstructor
public class ExtractFileTypeImpls implements ExtractFileType {

    @Override
    public @NotNull FileType invoke(@NotNull Path file) throws IOException {
        try (InputStream fs = Files.newInputStream(file); BufferedInputStream bis = new BufferedInputStream(fs)) {
            FileType fileType = FileTypeDetector.detectFileType(bis);
            return fileType;
        }
    }
}
