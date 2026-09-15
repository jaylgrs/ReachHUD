package main.reachhud.target;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;

public final class TargetTracker {

    private static Entity currentTarget;

    private TargetTracker() {
    }

    public static void update(Minecraft client) {
        if (client.player == null) {
            currentTarget = null;
            return;
        }

        currentTarget = client.crosshairPickEntity;
    }

    public static Entity getCurrentTarget() {
        return currentTarget;
    }
}