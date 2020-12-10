package net.okocraft.scoreboard.command;

import com.github.siroshun09.mccommand.common.AbstractCommand;
import com.github.siroshun09.mccommand.common.Command;
import com.github.siroshun09.mccommand.common.CommandResult;
import com.github.siroshun09.mccommand.common.SubCommandHolder;
import com.github.siroshun09.mccommand.common.context.CommandContext;
import com.github.siroshun09.mcmessage.MessageReceiver;
import net.okocraft.scoreboard.ScoreboardPlugin;
import net.okocraft.scoreboard.locale.DefaultMessage;
import net.okocraft.scoreboard.locale.Placeholders;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class BoardCommand extends AbstractCommand {

    private final ScoreboardPlugin plugin;
    private final SubCommandHolder subCommandHolder;

    public BoardCommand(@NotNull ScoreboardPlugin plugin) {
        super("board", "scoreboard.command", Set.of("sb"));

        this.plugin = plugin;
        this.subCommandHolder = SubCommandHolder.of();
    }

    @Override
    public @NotNull CommandResult onExecution(@NotNull CommandContext context) {
        var sender = context.getSender();

        if (!sender.hasPermission(getPermission())) {
            plugin.getMessageBuilder()
                    .getMessageWithPrefix(DefaultMessage.NO_PERMISSION, sender)
                    .replace(Placeholders.PERMISSION, this)
                    .send(sender);
            return CommandResult.NO_PERMISSION;
        }

        var args = context.getArguments();

        if (!args.isEmpty()) {
            var firstArg = args.get(0);
            var subCommand = subCommandHolder.searchOptional(firstArg);

            if (subCommand.isPresent()) {
                return subCommand.get().onExecution(context);
            } else if (!firstArg.get().equalsIgnoreCase("help")) {
                plugin.getMessageBuilder()
                        .getMessageWithPrefix(DefaultMessage.SUBCOMMAND_NOT_FOUND, sender)
                        .replace(Placeholders.COMMAND, firstArg)
                        .send(sender);
            }
        }

        sendHelp(sender);
        return CommandResult.SUCCESS;
    }

    @Override
    public @NotNull List<String> onTabCompletion(@NotNull CommandContext context) {
        var sender = context.getSender();
        var args = context.getArguments();

        if (!sender.hasPermission(getPermission()) || args.isEmpty()) {
            return Collections.emptyList();
        }

        var firstArg = args.get(0);

        if (args.size() == 1) {
            return subCommandHolder.getSubCommands()
                    .stream()
                    .filter(cmd -> sender.hasPermission(cmd.getPermission()))
                    .map(Command::getName)
                    .filter(cmd -> cmd.startsWith(firstArg.get()))
                    .collect(Collectors.toUnmodifiableList());
        }

        var subCommand = subCommandHolder.searchOptional(firstArg);

        return subCommand.map(cmd -> cmd.onTabCompletion(context)).orElse(Collections.emptyList());
    }

    private void sendHelp(@NotNull MessageReceiver receiver) {
    }
}
