package com.makrozai.eligiusnametag.domain.port;

import java.util.List;
import java.util.UUID;

public interface PlatformPort {
    void prepareTick();
    void endTick();
    List<UUID> getOnlinePlayers();
    List<UUID> getTamedMobs();
    void addTamedMob(UUID uuid);
    void removeTamedMob(UUID uuid);
    boolean canViewerSeeTarget(UUID viewerId, UUID targetId);
    boolean isSameWorld(UUID a, UUID b);
    double getDistanceSquared(UUID a, UUID b);
    boolean isGloballyHidden(UUID targetId);
    /** True when the target player is sneaking (used for nametag height compensation). */
    boolean isSneaking(UUID targetId);
    boolean hasCustomName(UUID targetId);
    void disableVanillaNametag(UUID targetId);
    net.kyori.adventure.text.Component parsePlaceholders(UUID targetId, String text);
    String getPrimaryGroup(UUID targetId);
    String getOwnerPrimaryGroup(UUID targetId);
    boolean hasPermission(UUID targetId, String permission);

    /**
     * True when the target is a player standing within {@code radius} blocks of the
     * spawn point of the world they are currently in. Non-player targets are never
     * considered "in spawn".
     */
    boolean isInSpawn(UUID targetId, double radius);
}
