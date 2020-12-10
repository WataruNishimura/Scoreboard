package net.okocraft.scoreboard.locale;

import org.jetbrains.annotations.NotNull;

public enum DefaultMessage implements com.github.siroshun09.mcmessage.message.DefaultMessage {
    ;

    private final String key;
    private final String def;

    DefaultMessage(@NotNull String key, @NotNull String def) {
        this.key = key;
        this.def = def;
    }

    @Override
    public @NotNull String getKey() {
        return key;
    }

    @Override
    public @NotNull String get() {
        return def;
    }
}
