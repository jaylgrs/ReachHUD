package main.reachhud.input;

import com.mojang.blaze3d.platform.InputConstants;
import main.reachhud.ReachHUD;
import main.reachhud.config.ReachHudConfig;
import main.reachhud.hud.ReachHudNotification;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public final class ReachHudKeybind {

    private static final KeyMapping.Category REACHHUD_CATEGORY =
            KeyMapping.Category.register(
                    Identifier.fromNamespaceAndPath(
                            ReachHUD.MOD_ID,
                            "reachhud"
                    )
            );

    private static KeyMapping toggleKey;

    private ReachHudKeybind() {
    }

    public static void register() {
        if (toggleKey != null) {
            return;
        }

        toggleKey = KeyMappingHelper.registerKeyMapping(
                new KeyMapping(
                        "key.reachhud.toggle",
                        InputConstants.Type.KEYSYM,
                        GLFW.GLFW_KEY_RIGHT_CONTROL,
                        REACHHUD_CATEGORY
                )
        );
    }

    public static void update() {
        if (toggleKey == null) {
            return;
        }

        while (toggleKey.consumeClick()) {
            boolean newState = !ReachHudConfig.isEnabled();

            ReachHudConfig.setEnabled(newState);

            ReachHudNotification.show(newState);
        }
    }

    public static boolean isEnabled() {
        return ReachHudConfig.isEnabled();
    }
}