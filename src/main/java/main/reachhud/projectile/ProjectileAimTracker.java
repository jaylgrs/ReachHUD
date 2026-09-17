package main.reachhud.projectile;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class ProjectileAimTracker {

    private static final double MAX_AIM_DISTANCE = 64.0;

    /*
     * Vanilla crossbow arrow projectile speed.
     */
    private static final double CROSSBOW_ARROW_SPEED = 3.15;

    /*
     * Firework rocket initial projectile speed.
     *
     * Firework rockets use their own projectile movement,
     * so they are simulated separately from arrows.
     */
    private static final double FIREWORK_SPEED = 1.6;

    /*
     * Vanilla Multishot spread.
     */
    private static final double MULTISHOT_SPREAD_DEGREES = 10.0;

    private static Entity currentTarget;
    private static boolean willHit;
    private static double targetDistance = -1.0;

    private ProjectileAimTracker() {
    }

    public static void update(Minecraft client) {
        if (client.player == null || client.level == null) {
            clearState();
            return;
        }

        ProjectileWeaponState weaponState =
                ProjectileWeaponState.detect(client);

        if (!weaponState.isProjectileWeapon()) {
            clearState();
            return;
        }

        /*
         * A bow only has a projectile trajectory while it
         * is actually being drawn.
         */
        if (weaponState.isBow()
                && !weaponState.isUsingWeapon()) {
            clearState();
            return;
        }

        /*
         * A crossbow only has a projectile trajectory when
         * it already contains a loaded projectile.
         */
        if (weaponState.isCrossbow()
                && !weaponState.isLoaded()) {
            clearState();
            return;
        }

        Entity target =
                findAimedEntity(client);

        if (target == null) {
            clearState();
            return;
        }

        currentTarget = target;

        Vec3 startPosition =
                client.player.getEyePosition();

        Vec3 direction =
                client.player.getViewVector(1.0F);

        AABB targetBox =
                target.getBoundingBox()
                        .inflate(target.getPickRadius());

        /*
         * Firework Rocket uses completely separate
         * projectile physics.
         */
        if (weaponState.isFirework()) {
            willHit = simulateFirework(
                    startPosition,
                    direction,
                    targetBox
            );
        } else {
            double projectileSpeed =
                    getProjectileSpeed(weaponState);

            if (projectileSpeed <= 0.0) {
                clearState();
                return;
            }

            /*
             * Normal Arrow / Crossbow Arrow.
             */
            if (!weaponState.hasMultishot()) {
                willHit = simulateTrajectory(
                        startPosition,
                        direction,
                        projectileSpeed,
                        targetBox
                );
            } else {
                /*
                 * Multishot:
                 *
                 * Center
                 * -10 degrees
                 * +10 degrees
                 */
                willHit = simulateMultishot(
                        startPosition,
                        direction,
                        projectileSpeed,
                        targetBox
                );
            }
        }

        targetDistance =
                getDistanceToTarget(
                        startPosition,
                        target
                );
    }

    private static boolean simulateTrajectory(
            Vec3 startPosition,
            Vec3 direction,
            double projectileSpeed,
            AABB targetBox
    ) {
        Vec3 initialVelocity =
                direction.scale(projectileSpeed);

        ProjectilePhysics.TrajectoryResult result =
                ProjectilePhysics.simulate(
                        startPosition,
                        initialVelocity,
                        targetBox
                );

        return result.hit();
    }

    private static boolean simulateFirework(
            Vec3 startPosition,
            Vec3 direction,
            AABB targetBox
    ) {
        Vec3 initialVelocity =
                direction.scale(FIREWORK_SPEED);

        FireworkPhysics.TrajectoryResult result =
                FireworkPhysics.simulate(
                        startPosition,
                        initialVelocity,
                        targetBox
                );

        return result.hit();
    }

    private static boolean simulateMultishot(
            Vec3 startPosition,
            Vec3 direction,
            double projectileSpeed,
            AABB targetBox
    ) {
        /*
         * Center projectile.
         */
        if (simulateTrajectory(
                startPosition,
                direction,
                projectileSpeed,
                targetBox
        )) {
            return true;
        }

        /*
         * Left projectile.
         */
        Vec3 leftDirection =
                rotateAroundY(
                        direction,
                        -MULTISHOT_SPREAD_DEGREES
                );

        if (simulateTrajectory(
                startPosition,
                leftDirection,
                projectileSpeed,
                targetBox
        )) {
            return true;
        }

        /*
         * Right projectile.
         */
        Vec3 rightDirection =
                rotateAroundY(
                        direction,
                        MULTISHOT_SPREAD_DEGREES
                );

        return simulateTrajectory(
                startPosition,
                rightDirection,
                projectileSpeed,
                targetBox
        );
    }

    private static Vec3 rotateAroundY(
            Vec3 direction,
            double degrees
    ) {
        double radians =
                Math.toRadians(degrees);

        double cos =
                Math.cos(radians);

        double sin =
                Math.sin(radians);

        double x =
                direction.x * cos
                        - direction.z * sin;

        double z =
                direction.x * sin
                        + direction.z * cos;

        return new Vec3(
                x,
                direction.y,
                z
        ).normalize();
    }

    private static double getProjectileSpeed(
            ProjectileWeaponState weaponState
    ) {
        if (weaponState.isBow()) {
            return calculateBowVelocity(
                    weaponState.getDrawProgress()
            );
        }

        if (weaponState.isCrossbow()
                && weaponState.isArrow()) {
            return CROSSBOW_ARROW_SPEED;
        }

        return 0.0;
    }

    private static double calculateBowVelocity(
            float drawProgress
    ) {
        float power = drawProgress;

        power =
                (power * power + power * 2.0F)
                        / 3.0F;

        if (power > 1.0F) {
            power = 1.0F;
        }

        if (power < 0.1F) {
            return 0.0;
        }

        return power * 3.0;
    }

    private static double getDistanceToTarget(
            Vec3 startPosition,
            Entity target
    ) {
        AABB targetBox =
                target.getBoundingBox()
                        .inflate(target.getPickRadius());

        return startPosition.distanceTo(
                targetBox.getCenter()
        );
    }

    private static Entity findAimedEntity(
            Minecraft client
    ) {
        Vec3 start =
                client.player.getEyePosition();

        Vec3 direction =
                client.player.getViewVector(1.0F);

        Entity closestEntity = null;
        double closestDistance =
                MAX_AIM_DISTANCE;

        AABB searchBox =
                client.player
                        .getBoundingBox()
                        .inflate(MAX_AIM_DISTANCE);

        for (Entity entity :
                client.player.level().getEntities(
                        client.player,
                        searchBox,
                        entity -> entity.isPickable()
                                && entity.isAlive()
                                && entity != client.player
                )) {

            AABB entityBox =
                    entity.getBoundingBox()
                            .inflate(entity.getPickRadius());

            double distance =
                    findRayIntersection(
                            start,
                            direction,
                            entityBox
                    );

            if (distance >= 0
                    && distance < closestDistance) {

                closestDistance = distance;
                closestEntity = entity;
            }
        }

        return closestEntity;
    }

    private static double findRayIntersection(
            Vec3 start,
            Vec3 direction,
            AABB box
    ) {
        Vec3 end =
                start.add(
                        direction.scale(
                                MAX_AIM_DISTANCE
                        )
                );

        java.util.Optional<Vec3> hit =
                box.clip(start, end);

        if (hit.isEmpty()) {
            return -1.0;
        }

        return start.distanceTo(hit.get());
    }

    public static Entity getCurrentTarget() {
        return currentTarget;
    }

    public static boolean hasTarget() {
        return currentTarget != null;
    }

    public static boolean willHit() {
        return willHit;
    }

    public static double getTargetDistance() {
        return targetDistance;
    }

    public static void clearTarget() {
        clearState();
    }

    private static void clearState() {
        currentTarget = null;
        willHit = false;
        targetDistance = -1.0;
    }
}