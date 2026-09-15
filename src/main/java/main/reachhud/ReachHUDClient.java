package main.reachhud;

import main.reachhud.config.ReachHudConfig;
import main.reachhud.hud.ReachHudRenderer;
import main.reachhud.input.ReachHudKeybind;
import main.reachhud.target.TargetTracker;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public class ReachHUDClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ReachHudConfig.load();

        ReachHudKeybind.register();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            ReachHudKeybind.update();
            TargetTracker.update(client);
        });

        ReachHudRenderer.register();
    }
}