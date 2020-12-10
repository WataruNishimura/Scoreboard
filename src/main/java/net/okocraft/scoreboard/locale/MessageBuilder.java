package net.okocraft.scoreboard.locale;

import com.github.siroshun09.mccommand.common.Command;
import com.github.siroshun09.mcmessage.MessageReceiver;
import com.github.siroshun09.mcmessage.builder.PlainTextBuilder;
import net.okocraft.scoreboard.ScoreboardPlugin;
import org.jetbrains.annotations.NotNull;

public class MessageBuilder {

    private final ScoreboardPlugin plugin;

    public MessageBuilder(@NotNull ScoreboardPlugin plugin) {
        this.plugin = plugin;
    }

    public @NotNull PlainTextBuilder getMessage(@NotNull DefaultMessage msg, @NotNull MessageReceiver receiver) {
        return plugin.getLanguageManager()
                .getMessage(msg, receiver)
                .toPlainTextBuilder()
                .setColorize(true);
    }

    public @NotNull PlainTextBuilder getMessageWithPrefix(@NotNull DefaultMessage msg, @NotNull MessageReceiver receiver) {
        return getMessage(msg, receiver)
                .addPrefix(
                        plugin.getLanguageManager().getMessage(DefaultMessage.PREFIX, receiver)
                );
    }

    public void sendMessage(@NotNull DefaultMessage message, @NotNull MessageReceiver receiver) {
        getMessage(message, receiver).send(receiver);
    }

    public void sendMessageWithPrefix(@NotNull DefaultMessage message, @NotNull MessageReceiver receiver) {
        getMessageWithPrefix(message, receiver).send(receiver);
    }

    public void sendNoPermission(@NotNull MessageReceiver receiver, @NotNull Command command) {
        getMessageWithPrefix(DefaultMessage.NO_PERMISSION, receiver)
                .replace(Placeholders.PERMISSION, command)
                .send(receiver);
    }
}
