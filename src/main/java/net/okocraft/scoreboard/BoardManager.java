package net.okocraft.scoreboard;

import com.github.siroshun09.configapi.bukkit.BukkitConfig;
import com.github.siroshun09.configapi.bukkit.BukkitYaml;
import net.okocraft.scoreboard.board.Board;
import net.okocraft.scoreboard.board.Line;
import net.okocraft.scoreboard.player.BoardDisplay;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class BoardManager {

    private final ScoreboardPlugin plugin;
    private final Map<String, Board> boards;
    private final Set<BoardDisplay> displayedBoards;

    BoardManager(@NotNull ScoreboardPlugin plugin) throws IOException {
        this.plugin = plugin;
        this.displayedBoards = new HashSet<>();
        boards = loadBoards();
    }

    public void showAllDefault() {
        plugin.getServer().getOnlinePlayers().forEach(this::showDefault);
    }

    public void showDefault(@NotNull Player player) {
        showBoard(player, "default");
    }

    public void showBoard(@NotNull Player player, @NotNull String boardName) {
        Board board = boards.get(boardName);

        if (board == null) {
            return;
        }

        displayedBoards.add(new BoardDisplay(plugin, board, player));
    }

    public void removeBoard(@NotNull Player player) {
        Set<BoardDisplay> displayed = displayedBoards.stream().filter(b -> b.getPlayer().equals(player)).collect(Collectors.toSet());
        displayed.forEach(displayedBoards::remove);
        player.setScoreboard(plugin.getScoreboardManager().getMainScoreboard());
    }

    public void removeAll() {
        for (BoardDisplay displayed : displayedBoards) {
            Player player = displayed.getPlayer();
            if (player.isOnline()) {
                player.setScoreboard(plugin.getScoreboardManager().getMainScoreboard());
            }
        }

        displayedBoards.clear();
    }

    public void update() {
        boards.values().forEach(Board::update);
        displayedBoards.forEach(BoardDisplay::update);
    }

    @NotNull
    @Unmodifiable
    private Map<String, Board> loadBoards() throws IOException {
        Board defBoard = loadBoard(new BukkitConfig(plugin, "default.yml", true));

        if (defBoard != null) {
            return Map.of("default", defBoard);
        } else {
            return Collections.emptyMap();
        }
        /*

        Path dirPath = plugin.getDataFolder().toPath().resolve("boards");

        if (!Files.exists(dirPath)) {
            if (defBoard != null) {
                return Map.of("default", defBoard);
            } else {
                return Collections.emptyMap();
            }
        }

        Set<Path> boardFiles =
                Files.list(plugin.getDataFolder().toPath().resolve("boards"))
                        .filter(Files::isRegularFile)
                        .filter(Files::isReadable)
                        .filter(p -> p.toString().endsWith(".yml"))
                        .collect(Collectors.toUnmodifiableSet());

        Map<String, Board> result = new HashMap<>();

        result.put("default", defBoard);

        for (Path file : boardFiles) {
            Board board = loadBoard(new BukkitYaml(file));

            if (board == null) {
                continue;
            }

            String name = file.getFileName().toString().replace(".yml", "");
            result.put(name, board);
        }

        return Map.copyOf(result);
         */
    }

    @Nullable
    private Board loadBoard(@NotNull BukkitYaml yaml) {
        if (!yaml.load()) {
            return null;
        }

        List<String> titleList = yaml.getStringList("title.list");

        if (titleList.isEmpty()) {
            return null;
        }

        Line title = new Line(titleList, yaml.getLong("title.interval", 5));

        ConfigurationSection section = yaml.getConfig().getConfigurationSection("line");
        if (section == null) {
            return null;
        }

        List<Line> lines = new LinkedList<>();
        for (String root : section.getKeys(false)) {
            List<String> lineList = section.getStringList(root + ".list");

            if (!lineList.isEmpty()) {
                lines.add(new Line(lineList, section.getLong(root + ".interval")));
            } else {
                lines.add(Line.EMPTY);
            }
        }

        return new Board(title, lines);
    }
}
