package main.reachhud.hud;

import main.reachhud.ReachHUD;
import main.reachhud.input.ReachHudKeybind;
import main.reachhud.projectile.ProjectileAimTracker;
import main.reachhud.reach.ReachCalculator;
import main.reachhud.target.TargetTracker;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;

public final class ReachHudRenderer {

    private static final double SMOOTH_SPEED = 0.25;

    private static Entity lastTarget;
    private static double displayedDistance = -1.0;

    private static Entity lastProjectileTarget;
    private static double displayedProjectileDistance = -1.0;

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
            resetSmoothing();
            return;
        }

        if (client.screen != null) {
            resetSmoothing();
            return;
        }

        // Notification must render even when ReachHUD is disabled.
        renderNotification(graphics, client);

        // Stop rendering the actual ReachHUD when disabled.
        if (!ReachHudKeybind.isEnabled()) {
            resetSmoothing();
            return;
        }

        /*
         * Projectile HUD
         *
         * Render this separately from the normal melee ReachHUD.
         * This allows Bow/Crossbow aiming to use its own target
         * detection and trajectory result.
         */
        if (ProjectileAimTracker.hasTarget()) {
            renderProjectileHud(graphics, client);
            return;
        }

        resetProjectileSmoothing();

        /*
         * Normal melee ReachHUD
         */
        Entity target = TargetTracker.getCurrentTarget();

        if (target == null) {
            resetSmoothing();
            return;
        }

        double distance = ReachCalculator.getDistanceTo(target);
        double reach = ReachCalculator.getPlayerReach();

        if (distance < 0 || reach < 0) {
            resetSmoothing();
            return;
        }

        // Keep the HUD visible for up to 3 blocks beyond actual reach.
        if (distance > reach + 3.0) {
            resetSmoothing();
            return;
        }

        boolean withinReach = distance <= reach;

        updateSmoothedDistance(target, distance);

        String targetName = target.getName().getString();
        String reachLabel = "Reach: ";
        String distanceText = String.format("%.2f", displayedDistance);

        int screenWidth = client.getWindow().getGuiScaledWidth();
        int screenHeight = client.getWindow().getGuiScaledHeight();

        int targetNameWidth = client.font.width(targetName);
        int reachLabelWidth = client.font.width(reachLabel);
        int distanceWidth = client.font.width(distanceText);

        int reachTotalWidth = reachLabelWidth + distanceWidth;

        int centerX = screenWidth / 2;
        int centerY = screenHeight / 2;

        int targetY = centerY + 11;
        int reachY = centerY + 23;

        int targetX = centerX - targetNameWidth / 2;
        int reachX = centerX - reachTotalWidth / 2;

        int targetNameColor = getTargetNameColor(target);

        int distanceColor = withinReach
                ? 0xFF55FF55
                : 0xFFFF5555;

        /*
         * Reach background only.
         */
        int reachPaddingHorizontal = 5;
        int reachPaddingVertical = 2;

        int reachBoxX = reachX - reachPaddingHorizontal;
        int reachBoxY = reachY - reachPaddingVertical;

        int reachBoxWidth =
                reachTotalWidth + reachPaddingHorizontal * 2;

        int reachTextHeight = 9;

        int reachBoxHeight =
                reachTextHeight + reachPaddingVertical * 2;

        graphics.fill(
                reachBoxX,
                reachBoxY,
                reachBoxX + reachBoxWidth,
                reachBoxY + reachBoxHeight,
                0x66000000
        );

        // Target name
        graphics.text(
                client.font,
                targetName,
                targetX,
                targetY,
                targetNameColor,
                true
        );

        // Reach label
        graphics.text(
                client.font,
                reachLabel,
                reachX,
                reachY,
                0xFFCCCCCC,
                true
        );

        // Reach distance
        graphics.text(
                client.font,
                distanceText,
                reachX + reachLabelWidth,
                reachY,
                distanceColor,
                true
        );
    }

    private static void renderProjectileHud(
            GuiGraphicsExtractor graphics,
            Minecraft client
    ) {
        Entity target = ProjectileAimTracker.getCurrentTarget();

        if (target == null) {
            resetProjectileSmoothing();
            return;
        }

        double distance = ProjectileAimTracker.getTargetDistance();

        if (distance < 0) {
            resetProjectileSmoothing();
            return;
        }

        boolean willHit = ProjectileAimTracker.willHit();

        updateSmoothedProjectileDistance(target, distance);

        String targetName = target.getName().getString();
        String aimLabel = "Aim: ";
        String distanceText =
                String.format("%.2f", displayedProjectileDistance);

        int screenWidth = client.getWindow().getGuiScaledWidth();
        int screenHeight = client.getWindow().getGuiScaledHeight();

        int targetNameWidth = client.font.width(targetName);
        int aimLabelWidth = client.font.width(aimLabel);
        int distanceWidth = client.font.width(distanceText);

        int aimTotalWidth =
                aimLabelWidth + distanceWidth;

        int centerX = screenWidth / 2;
        int centerY = screenHeight / 2;

        int targetY = centerY + 11;
        int aimY = centerY + 23;

        int targetX =
                centerX - targetNameWidth / 2;

        int aimX =
                centerX - aimTotalWidth / 2;

        int targetNameColor =
                getTargetNameColor(target);

        /*
         * Prediction result remains instant.
         *
         * Only the displayed distance is smoothed.
         */
        int distanceColor = willHit
                ? 0xFF55FF55
                : 0xFFFF5555;

        /*
         * Aim background.
         */
        int paddingHorizontal = 5;
        int paddingVertical = 2;

        int boxX =
                aimX - paddingHorizontal;

        int boxY =
                aimY - paddingVertical;

        int boxWidth =
                aimTotalWidth
                        + paddingHorizontal * 2;

        int textHeight = 9;

        int boxHeight =
                textHeight
                        + paddingVertical * 2;

        graphics.fill(
                boxX,
                boxY,
                boxX + boxWidth,
                boxY + boxHeight,
                0x66000000
        );

        // Target name
        graphics.text(
                client.font,
                targetName,
                targetX,
                targetY,
                targetNameColor,
                true
        );

        // Aim label
        graphics.text(
                client.font,
                aimLabel,
                aimX,
                aimY,
                0xFFCCCCCC,
                true
        );

        // Projectile distance
        graphics.text(
                client.font,
                distanceText,
                aimX + aimLabelWidth,
                aimY,
                distanceColor,
                true
        );
    }

    private static int getTargetNameColor(Entity target) {
        if (target instanceof Player || target instanceof Enemy) {
            return 0xFFFF5555;
        }

        return 0xFF55FF55;
    }

    private static void updateSmoothedDistance(
            Entity target,
            double actualDistance
    ) {
        if (lastTarget != target || displayedDistance < 0) {
            lastTarget = target;
            displayedDistance = actualDistance;
            return;
        }

        displayedDistance +=
                (actualDistance - displayedDistance) * SMOOTH_SPEED;

        if (Math.abs(actualDistance - displayedDistance) < 0.005) {
            displayedDistance = actualDistance;
        }
    }

    private static void updateSmoothedProjectileDistance(
            Entity target,
            double actualDistance
    ) {
        if (lastProjectileTarget != target
                || displayedProjectileDistance < 0) {

            lastProjectileTarget = target;
            displayedProjectileDistance = actualDistance;
            return;
        }

        displayedProjectileDistance +=
                (actualDistance - displayedProjectileDistance)
                        * SMOOTH_SPEED;

        if (Math.abs(
                actualDistance - displayedProjectileDistance
        ) < 0.005) {

            displayedProjectileDistance = actualDistance;
        }
    }

    private static void resetSmoothing() {
        lastTarget = null;
        displayedDistance = -1.0;
        resetProjectileSmoothing();
    }

    private static void resetProjectileSmoothing() {
        lastProjectileTarget = null;
        displayedProjectileDistance = -1.0;
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