package net.okocraft.scoreboard.locale;

import com.github.siroshun09.mcmessage.MessageReceiver;
import com.github.siroshun09.mcmessage.loader.LanguageLoader;
import com.github.siroshun09.mcmessage.message.KeyedMessage;
import com.github.siroshun09.mcmessage.message.Message;
import com.github.siroshun09.mcmessage.translation.Translation;
import com.github.siroshun09.mcmessage.translation.TranslationRegistry;
import com.github.siroshun09.mcmessage.util.InvalidMessage;
import net.okocraft.scoreboard.ScoreboardPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LanguageManager {

    private static final String FILE_EXTENSION = ".properties";
    private static final Locale DEFAULT_LOCALE =
            Objects.requireNonNullElseGet(Translation.parseLocale("en_US"), Locale::getDefault);

    private final ScoreboardPlugin plugin;
    private final Path directory;
    private final TranslationRegistry registry;

    public LanguageManager(@NotNull ScoreboardPlugin plugin) {
        this.plugin = plugin;
        this.directory = plugin.getDataFolder().toPath().resolve("lang");
        this.registry = TranslationRegistry.create();
    }

    @NotNull
    public Message getMessage(@NotNull DefaultMessage msg, @NotNull MessageReceiver receiver) {
        return registry.getMessage(msg, receiver.getLocale());
    }

    public void reload() throws IOException {
        if (!Files.isDirectory(directory)) {
            Files.createDirectories(directory);
        }

        registry.register(loadDefaultLanguage());
        loadCustomLanguageFiles();

        plugin.getLogger().info(
                "Loaded languages: " + registry.getTranslations()
                        .stream()
                        .map(Translation::getLocale)
                        .map(Locale::toString)
                        .collect(Collectors.joining(", ")));
    }

    private @NotNull Translation loadDefaultLanguage() throws IOException {
        Locale locale = DEFAULT_LOCALE;
        Path defFile = directory.resolve(locale.toString() + FILE_EXTENSION);

        if (Files.exists(defFile)) {
            return getLoadedLanguageFile(defFile).toTranslation(locale);
        } else {
            try (BufferedWriter writer = Files.newBufferedWriter(defFile, StandardCharsets.UTF_8, StandardOpenOption.CREATE)) {
                StringBuilder builder = new StringBuilder();
                for (KeyedMessage defMsg : DefaultMessage.values()) {
                    builder.setLength(0);
                    builder.append(defMsg.getKey()).append('=').append(defMsg.get());
                    writer.write(builder.toString());
                    writer.newLine();
                }
            }

            return Translation.of(locale, Set.of(DefaultMessage.values()));
        }
    }

    private void loadCustomLanguageFiles() throws IOException {
        try (Stream<Path> files = Files.list(directory)) {
            files.filter(Files::isRegularFile)
                    .filter(Files::isReadable)
                    .filter(p -> p.getFileName().toString().endsWith(FILE_EXTENSION))
                    .filter(p -> !p.getFileName().toString().equals(DEFAULT_LOCALE.toString() + FILE_EXTENSION))
                    .map(this::getLoadedLanguageFileUnsafe)
                    .filter(Objects::nonNull)
                    .map(LanguageLoader::toTranslation)
                    .filter(Objects::nonNull)
                    .forEach(registry::register);
        }
    }

    private LanguageLoader getLoadedLanguageFile(@NotNull Path path) throws IOException {
        LanguageLoader loader = LanguageLoader.fromPropertiesFile(path);
        loader.load().forEach(this::printInvalidMessage);
        return loader;
    }

    @Nullable
    private LanguageLoader getLoadedLanguageFileUnsafe(@NotNull Path path) {
        try {
            return getLoadedLanguageFile(path);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not load " + path.getFileName().toString(), e);
            return null;
        }
    }

    private void printInvalidMessage(@NotNull InvalidMessage invalid) {
        plugin.getLogger().warning("Invalid message: " + invalid.toString());
    }
}
