package main.reachhud.hud;

import main.reachhud.ReachHUD;
import main.reachhud.input.ReachHudKeybind;
import main.reachhud.reach.ReachCalculator;
import main.reachhud.target.TargetTracker;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.Entity;

public final class ReachHudRenderer {

    private ReachHudRenderer() {
    }

    public static void register() {
        HudElementRegistry.attachElementAfter(
                VanillaHudElements.CROSSHAIR,
                ReachHUD.id("reach_hud"),
                ReachHudRenderer::render
        );
    }

    private static void render(
            GuiGraphicsExtractor graphics,
            DeltaTracker deltaTracker
    ) {
        Minecraft client = Minecraft.getInstance();

        if (client.player == null || client.options.hideGui) {
            return;
        }

        if (client.screen != null) {
            return;
        }

        // Notification must render even when ReachHUD is disabled.
        renderNotification(graphics, client);

        // Stop rendering the actual ReachHUD when disabled.
        if (!ReachHudKeybind.isEnabled()) {
            return;
        }

        Entity target = TargetTracker.getCurrentTarget();

        if (target == null) {
            return;
        }

        double distance = ReachCalculator.getDistanceTo(target);
        double reach = ReachCalculator.getPlayerReach();

        if (distance < 0 || reach < 0) {
            return;
        }

        boolean withinReach = distance <= reach;

        String targetName = target.getName().getString();
        String distanceText = String.format("%.2f blocks", distance);

        int screenWidth = client.getWindow().getGuiScaledWidth();
        int screenHeight = client.getWindow().getGuiScaledHeight();

        int targetNameWidth = client.font.width(targetName);
        int distanceWidth = client.font.width(distanceText);

        int targetX = (screenWidth - targetNameWidth) / 2;
        int distanceX = (screenWidth - distanceWidth) / 2;

        int centerY = screenHeight / 2;

        int targetY = centerY + 12;
        int distanceY = centerY + 23;

        int textColor = withinReach
                ? 0xFF55FF55
                : 0xFFFF5555;

        // Target name
        graphics.text(
                client.font,
                targetName,
                targetX,
                targetY,
                0xFFFFFFFF,
                true
        );

        // Reach distance
        graphics.text(
                client.font,
                distanceText,
                distanceX,
                distanceY,
                textColor,
                true
        );
    }

    private static void renderNotification(
            GuiGraphicsExtractor graphics,
            Minecraft client
    ) {
        if (!ReachHudNotification.isVisible()) {
            return;
        }

        String text = ReachHudNotification.isEnabled()
                ? "ReachHUD: Active"
                : "ReachHUD: Inactive";

        int screenWidth = client.getWindow().getGuiScaledWidth();
        int screenHeight = client.getWindow().getGuiScaledHeight();

        int textWidth = client.font.width(text);

        int x = (screenWidth - textWidth) / 2;

        // Above the hotbar
        int y = screenHeight - 58;

        int textColor = ReachHudNotification.isEnabled()
                ? 0xFF55FF55
                : 0xFFFF5555;

        graphics.text(
                client.font,
                text,
                x,
                y,
                textColor,
                true
        );
    }
}