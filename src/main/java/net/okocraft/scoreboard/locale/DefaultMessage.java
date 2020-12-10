package net.okocraft.scoreboard.locale;

import org.jetbrains.annotations.NotNull;

public enum DefaultMessage implements com.github.siroshun09.mcmessage.message.DefaultMessage {
    PREFIX("prefix", "&8[&6SB&8] &r"),

    NO_PERMISSION("no-permission", "You don't have the permission: %perm%"),
    SUBCOMMAND_NOT_FOUND("command-not-found", "The subcommand %cmd% was not found."),
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
