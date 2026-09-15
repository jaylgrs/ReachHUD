package main.reachhud.config;

import main.reachhud.ReachHUD;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class ReachHudConfig {

    private static final Path CONFIG_PATH =
            FabricLoader.getInstance()
                    .getConfigDir()
                    .resolve("reachhud.properties");

    private static final String HUD_ENABLED = "hud.enabled";

    private static boolean enabled = true;

    private ReachHudConfig() {
    }

    public static void load() {
        if (!Files.exists(CONFIG_PATH)) {
            save();
            return;
        }

        Properties properties = new Properties();

        try (InputStream input = Files.newInputStream(CONFIG_PATH)) {
            properties.load(input);

            enabled = Boolean.parseBoolean(
                    properties.getProperty(HUD_ENABLED, "true")
            );

        } catch (IOException exception) {
            enabled = true;

            ReachHUD.LOGGER.error(
                    "Failed to load ReachHUD configuration.",
                    exception
            );
        }
    }

    public static void save() {
        Properties properties = new Properties();

        properties.setProperty(
                HUD_ENABLED,
                Boolean.toString(enabled)
        );

        try {
            Files.createDirectories(CONFIG_PATH.getParent());

            try (OutputStream output = Files.newOutputStream(CONFIG_PATH)) {
                properties.store(
                        output,
                        "ReachHUD Configuration"
                );
            }

        } catch (IOException exception) {
            ReachHUD.LOGGER.error(
                    "Failed to save ReachHUD configuration.",
                    exception
            );
        }
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void setEnabled(boolean enabled) {
        ReachHudConfig.enabled = enabled;
        save();
    }
}