package net.okocraft.scoreboard.locale;

import com.github.siroshun09.mccommand.common.Command;
import com.github.siroshun09.mcmessage.replacer.FunctionalPlaceholder;

public final class Placeholders {

    public static final FunctionalPlaceholder<Command> PERMISSION =
            FunctionalPlaceholder.create("%perm%", Command::getPermission);
}
