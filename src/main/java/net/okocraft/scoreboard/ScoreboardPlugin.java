package net.okocraft.scoreboard;

import com.github.siroshun09.mccommand.bukkit.BukkitCommandFactory;
import com.github.siroshun09.mccommand.bukkit.paper.AsyncTabCompleteListener;
import com.github.siroshun09.mccommand.bukkit.paper.PaperChecker;
import net.okocraft.scoreboard.command.BoardCommand;
import net.okocraft.scoreboard.config.BoardManager;
import net.okocraft.scoreboard.config.Configuration;
import net.okocraft.scoreboard.display.manager.BukkitDisplayManager;
import net.okocraft.scoreboard.display.manager.DisplayManager;
import net.okocraft.scoreboard.display.manager.PacketDisplayManager;
import net.okocraft.scoreboard.external.PlaceholderAPIHooker;
import net.okocraft.scoreboard.external.ProtocolLibChecker;
import net.okocraft.scoreboard.listener.PlayerListener;
import net.okocraft.scoreboard.listener.PluginListener;
import net.okocraft.scoreboard.locale.LanguageManager;
import net.okocraft.scoreboard.locale.MessageBuilder;
import net.okocraft.scoreboard.task.UpdateTask;
import net.okocraft.scoreboard.util.LengthChecker;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class ScoreboardPlugin extends JavaPlugin {

    private static final long MILLISECONDS_PER_TICK = 50;

    private Configuration config;

    private BoardManager boardManager;
    private DisplayManager displayManager;
    private LanguageManager languageManager;
    private MessageBuilder messageBuilder;
    private PlayerListener playerListener;
    private PluginListener pluginListener;

    private ExecutorService executor;
    private ScheduledExecutorService scheduler;

    @Override
    public void onLoad() {
        config = new Configuration(this);

        LengthChecker.setLimit(config.getLengthLimit());

        languageManager = new LanguageManager(this);

        try {
            languageManager.reload();
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "Could not load languages.", e);
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        executor = Executors.newFixedThreadPool(config.getThreads());
        scheduler = Executors.newSingleThreadScheduledExecutor();
    }

    @Override
    public void onEnable() {
        boardManager = new BoardManager(this);

        messageBuilder = new MessageBuilder(this);

        playerListener = new PlayerListener(this);
        playerListener.register();

        pluginListener = new PluginListener(this);
        pluginListener.register();

        updateDisplayManager(ProtocolLibChecker.checkEnabled(getServer()));

        if (PlaceholderAPIHooker.checkEnabled(getServer())) {
            printPlaceholderIsAvailable();
        }

        var command = new BoardCommand(this);
        Optional.ofNullable(getCommand("board"))
                .ifPresent(pluginCommand -> BukkitCommandFactory.registerAsync(pluginCommand, command));

        if (PaperChecker.check()) {
            AsyncTabCompleteListener.register(this, command);
        }
    }

    @Override
    public void onDisable() {
        if (displayManager != null) {
            displayManager.hideAllBoards();
        }

        if (playerListener != null) {
            playerListener.unregister();
        }

        if (pluginListener != null) {
            pluginListener.unregister();
        }

        if (executor != null) {
            executor.shutdownNow();
        }

        if (scheduler != null) {
            scheduler.shutdownNow();
        }
    }

    @NotNull
    public BoardManager getBoardManager() {
        if (boardManager == null) {
            throw new IllegalStateException();
        }

        return boardManager;
    }

    public DisplayManager getDisplayManager() {
        if (boardManager == null) {
            throw new IllegalStateException();
        }

        return displayManager;
    }

    public @NotNull LanguageManager getLanguageManager() {
        return languageManager;
    }

    public @NotNull MessageBuilder getMessageBuilder() {
        return messageBuilder;
    }

    public void runAsync(@NotNull Runnable runnable) {
        executor.submit(runnable);
    }

    @NotNull
    public ScheduledFuture<?> scheduleUpdateTask(@NotNull UpdateTask task, long tick) {
        long interval = tick * MILLISECONDS_PER_TICK;
        return scheduler.scheduleWithFixedDelay(() -> runAsync(task), interval, interval, TimeUnit.MILLISECONDS);
    }

    public void updateDisplayManager(boolean isEnabledProtocolLib) {
        boolean useProtocolLib = config.isUsingProtocolLib() && isEnabledProtocolLib;

        if (displayManager != null && displayManager.isUsingProtocolLib() == useProtocolLib) {
            return;
        }

        if (useProtocolLib) {
            displayManager = new PacketDisplayManager(this);
            getLogger().info("We are using ProtocolLib.");
        } else {
            displayManager = new BukkitDisplayManager(this);
            getLogger().info("We are using Bukkit's Scoreboard.");
        }

        getServer().getOnlinePlayers().forEach(displayManager::showDefaultBoard);
    }

    public void printPlaceholderIsAvailable() {
        getLogger().info("PlaceholderAPI is available!");
    }
}
