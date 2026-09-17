package main.reachhud.projectile;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class FireworkPhysics {

    private static final double MAX_DISTANCE = 128.0;

    /*
     * Vanilla firework rocket movement.
     *
     * Each tick:
     *
     * horizontal velocity *= 1.15
     * vertical velocity += 0.04
     */
    private static final double HORIZONTAL_ACCELERATION = 1.15;
    private static final double VERTICAL_ACCELERATION = 0.04;

    /*
     * Small collision volume for the rocket.
     */
    private static final double PROJECTILE_HALF_SIZE = 0.125;

    private FireworkPhysics() {
    }

    public static TrajectoryResult simulate(
            Vec3 startPosition,
            Vec3 initialVelocity,
            AABB targetBox
    ) {
        if (startPosition == null
                || initialVelocity == null
                || targetBox == null) {
            return TrajectoryResult.miss();
        }

        Vec3 position = startPosition;
        Vec3 velocity = initialVelocity;

        double travelledDistance = 0.0;

        /*
         * Fireworks have a finite lifetime in vanilla,
         * but 200 ticks is a safe prediction limit.
         */
        for (int tick = 0; tick < 200; tick++) {

            Vec3 nextPosition =
                    position.add(velocity);

            /*
             * Check the complete movement segment.
             *
             * This prevents a fast-moving rocket from
             * skipping through an entity between ticks.
             */
            if (segmentIntersectsTarget(
                    position,
                    nextPosition,
                    targetBox
            )) {
                double hitDistance =
                        travelledDistance
                                + position.distanceTo(nextPosition);

                return TrajectoryResult.hit(
                        nextPosition,
                        hitDistance
                );
            }

            travelledDistance +=
                    position.distanceTo(nextPosition);

            if (travelledDistance >= MAX_DISTANCE) {
                break;
            }

            position = nextPosition;

            /*
             * Vanilla FireworkRocketEntity movement:
             *
             * X/Z velocity gets multiplied by 1.15.
             * Y velocity receives +0.04.
             */
            velocity = new Vec3(
                    velocity.x * HORIZONTAL_ACCELERATION,
                    velocity.y + VERTICAL_ACCELERATION,
                    velocity.z * HORIZONTAL_ACCELERATION
            );
        }

        return TrajectoryResult.miss();
    }

    private static boolean segmentIntersectsTarget(
            Vec3 start,
            Vec3 end,
            AABB targetBox
    ) {
        AABB expandedTarget =
                targetBox.inflate(
                        PROJECTILE_HALF_SIZE
                );

        return expandedTarget
                .clip(start, end)
                .isPresent();
    }

    public record TrajectoryResult(
            boolean hit,
            Vec3 hitPosition,
            double distance
    ) {

        public static TrajectoryResult hit(
                Vec3 hitPosition,
                double distance
        ) {
            return new TrajectoryResult(
                    true,
                    hitPosition,
                    distance
            );
        }

        public static TrajectoryResult miss() {
            return new TrajectoryResult(
                    false,
                    Vec3.ZERO,
                    -1.0
            );
        }
    }
}