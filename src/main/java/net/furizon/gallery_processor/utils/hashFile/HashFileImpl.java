package net.furizon.gallery_processor.utils.hashFile;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

@Slf4j
@Component
public class HashFileImpl implements HashFile {

    @Override
    public @NotNull UUID hashFile(@NotNull Path file) throws IOException {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            log.error("No MD5 algorithm available");
            throw new RuntimeException(e);
        }
        try (InputStream is = Files.newInputStream(file)) {
            byte[] buffer = new byte[64];
            int read = 0;
            while( ( read = is.read( buffer ) ) > 0 ){
                md.update(buffer);
            }
        }
        byte[] digest = md.digest();

        long msb = 0L;
        long lsb = 0L;
        for (int i=0; i<8; i++)
            msb = (msb << 8) | (digest[i] & 0xffL);
        for (int i=8; i<16; i++)
            lsb = (lsb << 8) | (digest[i] & 0xffL);
        return new UUID(msb, lsb);
    }
}
