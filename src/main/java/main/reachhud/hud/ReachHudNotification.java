package main.reachhud.hud;

public final class ReachHudNotification {

    private static boolean visible;
    private static boolean enabled;
    private static long hideAt;

    private static final long DISPLAY_TIME = 2000L;

    private ReachHudNotification() {
    }

    public static void show(boolean enabled) {
        ReachHudNotification.enabled = enabled;
        ReachHudNotification.visible = true;
        ReachHudNotification.hideAt =
                System.currentTimeMillis() + DISPLAY_TIME;
    }

    public static boolean isVisible() {
        if (!visible) {
            return false;
        }

        if (System.currentTimeMillis() >= hideAt) {
            visible = false;
            return false;
        }

        return true;
    }

    public static boolean isEnabled() {
        return enabled;
    }
}