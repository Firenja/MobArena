package com.garbagemule.MobArena.signs;

import com.garbagemule.MobArena.Messenger;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;

import java.util.logging.Level;
import java.util.logging.Logger;

class HandlesSignCreation2 implements Listener {

    private final SignCreator creator;
    private final SignWriter writer;
    private final SignStore2 store;
    private final Messenger messenger;
    private final Logger log;

    HandlesSignCreation2(
        SignCreator creator,
        SignWriter writer,
        SignStore2 store,
        Messenger messenger,
        Logger log
    ) {
        this.creator = creator;
        this.writer = writer;
        this.store = store;
        this.messenger = messenger;
        this.log = log;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void on(SignChangeEvent event) {
        ArenaSign sign;
        try {
            sign = creator.create(event);
            if (sign == null) {
                return;
            }
        } catch (IllegalArgumentException e) {
            messenger.tell(event.getPlayer(), e.getMessage());
            return;
        }

        try {
            writer.write(sign);
        } catch (Exception e) {
            messenger.tell(event.getPlayer(), "Sign creation failed:\n" + ChatColor.RED + e.getMessage());
            log.log(Level.SEVERE, "Failed to write arena sign to data file", e);
            return;
        }

        store.add(sign);

        String message = "New " + sign.type + " sign created for arena " + sign.arenaId;
        messenger.tell(event.getPlayer(), message);
        log.info(message);
    }

}
