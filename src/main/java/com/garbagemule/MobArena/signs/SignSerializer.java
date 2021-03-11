package com.garbagemule.MobArena.signs;

class SignSerializer {

    String serialize(ArenaSign sign) {
        String id = sign.location.getWorld().getUID().toString();
        String name = sign.location.getWorld().getName();

        String x = String.valueOf(sign.location.getBlockX());
        String y = String.valueOf(sign.location.getBlockY());
        String z = String.valueOf(sign.location.getBlockZ());

        String arenaId = sign.arenaId;
        String type = sign.type;
        String templateId = sign.templateId;

        return String.join(";", id, name, x, y, z, arenaId, type, templateId);
    }

    boolean equal(String line1, String line2) {
        if (line1.equals(line2)) {
            return true;
        }

        String[] parts1 = line1.split(";");
        String[] parts2 = line2.split(";");

        // World ID and (x,y,z) are all that matter
        return parts1[0].equals(parts2[0])
            && parts1[2].equals(parts2[2])
            && parts1[3].equals(parts2[3])
            && parts1[4].equals(parts2[4]);
    }

}
