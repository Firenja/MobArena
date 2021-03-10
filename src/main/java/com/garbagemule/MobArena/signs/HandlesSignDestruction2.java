package com.garbagemule.MobArena.signs;

import com.garbagemule.MobArena.Messenger;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.logging.Level;
import java.util.logging.Logger;

class HandlesSignDestruction2 implements Listener {

    private final SignStore2 store;
    private final SignWriter writer;
    private final Messenger messenger;
    private final Logger log;

    HandlesSignDestruction2(
        SignStore2 store,
        SignWriter writer,
        Messenger messenger,
        Logger log
    ) {
        this.store = store;
        this.writer = writer;
        this.messenger = messenger;
        this.log = log;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void on(BlockBreakEvent event) {
        Location location = event.getBlock().getLocation();

        ArenaSign sign = store.removeByLocation(location);
        if (sign == null) {
            return;
        }

        try {
            writer.erase(sign);
        } catch (Exception e) {
            messenger.tell(event.getPlayer(), "Sign destruction failed:\n" + ChatColor.RED + e.getMessage());
            log.log(Level.SEVERE, "Failed to erase arena sign from data file", e);
            return;
        }

        String message = "Removed " + sign.type + " sign for arena " + sign.arenaId;
        messenger.tell(event.getPlayer(), message);
        log.info(message);
    }

}
