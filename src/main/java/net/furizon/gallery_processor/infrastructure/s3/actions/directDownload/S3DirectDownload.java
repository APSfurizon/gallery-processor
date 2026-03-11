package net.furizon.gallery_processor.infrastructure.s3.actions.directDownload;

import com.drew.lang.annotations.NotNull;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

import java.io.InputStream;
import java.nio.file.Path;

public interface S3DirectDownload {
    void toFile(@NotNull String key, @NotNull Path file) throws NoSuchKeyException;
    byte[] toBytes(@NotNull String key) throws NoSuchKeyException;
    InputStream toInputStream(@org.jetbrains.annotations.NotNull String key) throws NoSuchKeyException;
}
