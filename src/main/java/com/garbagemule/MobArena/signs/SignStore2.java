package com.garbagemule.MobArena.signs;

import org.bukkit.Location;

import java.util.HashMap;
import java.util.Map;

class SignStore2 {

    private final Map<Location, ArenaSign> signs = new HashMap<>();

    void add(ArenaSign sign) {
        signs.put(sign.location, sign);
    }

    ArenaSign removeByLocation(Location location) {
        return signs.remove(location);
    }

}
