package com.makrozai.eligiusnametag.adapter.platform;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.entity.Tameable;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Unit-tests the bare {@code <head>} name resolution for players and pet owners.
 * Covers Paper 1.21.9+ behaviour where entity owner profiles are not auto-resolved:
 * an online owner must win over the (possibly null) profile name.
 */
public class PetHeadNameTest {

    private final UUID ownerId = UUID.randomUUID();

    private Tameable pet(org.bukkit.OfflinePlayer owner) {
        Tameable t = Mockito.mock(Tameable.class);
        Mockito.when(t.getOwner()).thenReturn(owner);
        Mockito.when(owner.getUniqueId()).thenReturn(ownerId);
        return t;
    }

    @Test
    public void testPlayerTarget_ResolvesOwnName() {
        Player p = Mockito.mock(Player.class);
        Mockito.when(p.getName()).thenReturn("Steve");
        assertEquals("Steve", PaperPlatformAdapter.resolveSelfHeadName(p, null));
    }

    @Test
    public void testOnlineOwner_WinsOverProfileName() {
        Player owner = Mockito.mock(Player.class);
        Mockito.when(owner.getName()).thenReturn("OnlineOwner");

        try (MockedStatic<Bukkit> bukkit = Mockito.mockStatic(Bukkit.class)) {
            bukkit.when(() -> Bukkit.getPlayer(ownerId)).thenReturn(owner);
            assertEquals("OnlineOwner", PaperPlatformAdapter.resolveSelfHeadName(pet(owner), "WhateverProfile"));
        }
    }

    @Test
    public void testOfflineOwner_FallsBackToProfileName() {
        org.bukkit.OfflinePlayer owner = Mockito.mock(org.bukkit.OfflinePlayer.class);

        try (MockedStatic<Bukkit> bukkit = Mockito.mockStatic(Bukkit.class)) {
            bukkit.when(() -> Bukkit.getPlayer(ownerId)).thenReturn(null);
            // Paper 1.21.9+ can report a null profile name for offline owners.
            Mockito.when(owner.getName()).thenReturn(null);
            assertEquals("KnownName", PaperPlatformAdapter.resolveSelfHeadName(pet(owner), "KnownName"));
        }
    }

    @Test
    public void testUnknownOwner_ReturnsNull() {
        org.bukkit.OfflinePlayer owner = Mockito.mock(org.bukkit.OfflinePlayer.class);

        try (MockedStatic<Bukkit> bukkit = Mockito.mockStatic(Bukkit.class)) {
            bukkit.when(() -> Bukkit.getPlayer(ownerId)).thenReturn(null);
            Mockito.when(owner.getName()).thenReturn(null);
            // "Owner" is the placeholder fallback, not a real name: no rewrite.
            assertNull(PaperPlatformAdapter.resolveSelfHeadName(pet(owner), "Owner"));
        }
    }

    @Test
    public void testNullTarget_ReturnsNull() {
        assertNull(PaperPlatformAdapter.resolveSelfHeadName(null, null));
    }
}
