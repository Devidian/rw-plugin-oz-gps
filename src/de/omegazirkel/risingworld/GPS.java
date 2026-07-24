package de.omegazirkel.risingworld;

import java.nio.file.Path;

import de.omegazirkel.risingworld.tools.FileChangeListener;
import net.risingworld.api.events.EventMethod;
import net.risingworld.api.events.Listener;
import net.risingworld.api.events.player.PlayerCommandEvent;
import net.risingworld.api.events.player.PlayerDeathEvent;
import net.risingworld.api.events.player.PlayerSpawnEvent;

/** Rising World entry point; runtime behavior lives in {@link GPSRuntime}. */
public final class GPS extends GPSRuntime implements Listener, FileChangeListener {
    @Override
    public void onEnable() {
        super.onEnable();
        registerEventListener(this);
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    @Override
    public void onSettingsChanged(Path settingsPath) {
        super.onSettingsChanged(settingsPath);
    }

    @Override
    @EventMethod
    public void onPlayerCommand(PlayerCommandEvent event) {
        super.onPlayerCommand(event);
    }

    @Override
    @EventMethod
    public void onPlayerSpawnEvent(PlayerSpawnEvent event) {
        super.onPlayerSpawnEvent(event);
    }

    @Override
    @EventMethod
    public void onPlayerDeathEvent(PlayerDeathEvent event) {
        super.onPlayerDeathEvent(event);
    }
}
