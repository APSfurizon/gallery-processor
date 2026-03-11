package net.furizon.gallery_processor.utils.cmdExecutor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class CmdExecutorImpl implements CmdExecutor {

    @Value("${cmd-executor.timeout}")
    private long timeout;

    @Override
    public String execute(@NotNull String caller, @NotNull String... args) throws IOException {
        String uuid = UUID.randomUUID().toString();
        log.info("[{}-{}] Executing command `{}`", caller, uuid, String.join(" ", args));
        ProcessBuilder processBuilder = new ProcessBuilder(args);
        Process p = processBuilder.start();
        boolean terminated = false;
        try {
            terminated = p.waitFor(timeout, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            log.error("[{}-{}] interrupt exception", caller, uuid);
            throw new IOException(e);
        }
        if (!terminated) {
            log.error("[{}-{}] has timed out!", caller, uuid);
            throw new IOException("");
        }

        String stdout;
        try (InputStream is = p.getInputStream()) {
            stdout = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }

        if (p.exitValue() != 0) {
            log.error("[{}-{}] exited with code {}", caller, uuid, p.exitValue());
            throw new IOException("");
        }

        log.info("[{}-{}] Command result: `{}`", caller, uuid, stdout);
        return stdout;
    }
}
