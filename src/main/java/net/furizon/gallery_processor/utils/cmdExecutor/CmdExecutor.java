package net.furizon.gallery_processor.utils.cmdExecutor;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public interface CmdExecutor {
    String execute(@NotNull String caller, @NotNull String ... args) throws IOException;
}
