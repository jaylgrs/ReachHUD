package main.reachhud.reach;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class ReachCalculator {

    private ReachCalculator() {
    }

    public static double getDistanceTo(Entity entity) {
        Minecraft client = Minecraft.getInstance();

        if (client.player == null || entity == null) {
            return -1.0;
        }

        Vec3 eyePosition = client.player.getEyePosition();

        AABB targetBox = entity.getBoundingBox();

        return Math.sqrt(targetBox.distanceToSqr(eyePosition));
    }

    public static double getPlayerReach() {
        Minecraft client = Minecraft.getInstance();

        if (client.player == null) {
            return -1.0;
        }

        return client.player.getAttributeValue(
                net.minecraft.world.entity.ai.attributes.Attributes.ENTITY_INTERACTION_RANGE
        );
    }

    public static boolean isWithinReach(Entity entity) {
        double distance = getDistanceTo(entity);
        double reach = getPlayerReach();

        if (distance < 0 || reach < 0) {
            return false;
        }

        return distance <= reach;
    }
}