package net.okocraft.scoreboard.locale;

import com.github.siroshun09.mccommand.common.Command;
import com.github.siroshun09.mccommand.common.argument.Argument;
import com.github.siroshun09.mcmessage.replacer.FunctionalPlaceholder;

public final class Placeholders {

    public static final FunctionalPlaceholder<Command> PERMISSION =
            FunctionalPlaceholder.create("%perm%", Command::getPermission);
    public static final FunctionalPlaceholder<Argument> COMMAND =
            FunctionalPlaceholder.create("%cmd%", Argument::get);
}
