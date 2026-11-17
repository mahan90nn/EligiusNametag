package com.makrozai.eligiusnametag.adapter.config;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests the players.yml spawn-area section: accepted key spellings
 * (in-spawn / in_spawn / inspawn) and both shapes (simple list statement
 * and structured radius/default_format/groups form).
 */
public class SpawnConfigTest {

    private static YamlConfiguration yaml(String content) {
        return YamlConfiguration.loadConfiguration(
                new InputStreamReader(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8))));
    }

    @Test
    public void testHyphenatedKey_StructuredForm() {
        YamlConfiguration cfg = yaml(""
                + "default_format:\n"
                + "  - \"<white>normal</white>\"\n"
                + "in-spawn:\n"
                + "  radius: 32\n"
                + "  default_format:\n"
                + "    - \"<gold>WELCOME</gold>\"\n"
                + "    - \"<white><PLAYER></white>\"\n"
                + "  groups:\n"
                + "    admin:\n"
                + "      - \"<gold>[SPAWN]</gold>\"\n");
        assertTrue(YamlConfigAdapter.hasSpawnNametagConfig(cfg));
        assertEquals(32.0, YamlConfigAdapter.spawnRadius(cfg));
        assertEquals(Arrays.asList("<gold>WELCOME</gold>", "<white><PLAYER></white>"),
                YamlConfigAdapter.spawnNametagTemplate(cfg, null));
        assertEquals(Collections.singletonList("<gold>[SPAWN]</gold>"),
                YamlConfigAdapter.spawnNametagTemplate(cfg, "admin"));
    }

    @Test
    public void testHyphenatedKey_SimpleListStatement() {
        YamlConfiguration cfg = yaml(""
                + "in-spawn:\n"
                + "  - \"<gold>SPAWN TAG <PLAYER>\"\n");
        assertTrue(YamlConfigAdapter.hasSpawnNametagConfig(cfg));
        assertEquals(16.0, YamlConfigAdapter.spawnRadius(cfg)); // default radius
        assertEquals(Collections.singletonList("<gold>SPAWN TAG <PLAYER>"),
                YamlConfigAdapter.spawnNametagTemplate(cfg, null));
        // No groups inside a simple statement: every group falls back to the list
        assertEquals(Collections.singletonList("<gold>SPAWN TAG <PLAYER>"),
                YamlConfigAdapter.spawnNametagTemplate(cfg, "admin"));
    }

    @Test
    public void testUnderscoreAndLegacyKeys() {
        YamlConfiguration underscore = yaml("in_spawn:\n  - \"a\"\n");
        assertEquals(Collections.singletonList("a"), YamlConfigAdapter.spawnNametagTemplate(underscore, null));

        YamlConfiguration legacy = yaml("inspawn:\n  default_format:\n    - \"b\"\n");
        assertEquals(Collections.singletonList("b"), YamlConfigAdapter.spawnNametagTemplate(legacy, null));
        assertTrue(YamlConfigAdapter.hasSpawnNametagConfig(legacy));
    }

    @Test
    public void testMissingSection_Disabled() {
        YamlConfiguration cfg = yaml("default_format:\n  - \"<white>x</white>\"\n");
        assertFalse(YamlConfigAdapter.hasSpawnNametagConfig(cfg));
        assertTrue(YamlConfigAdapter.spawnNametagTemplate(cfg, null).isEmpty());
    }

    @Test
    public void testSingleStringForm_TreatedAsOneLine() {
        YamlConfiguration cfg = yaml("in-spawn: \"<gold>Solo</gold>\"\n");
        assertTrue(YamlConfigAdapter.hasSpawnNametagConfig(cfg));
        assertEquals(Collections.singletonList("<gold>Solo</gold>"),
                YamlConfigAdapter.spawnNametagTemplate(cfg, null));
    }

    @Test
    public void testGroupOverrideWinsOverDefault() {
        YamlConfiguration cfg = yaml(""
                + "in-spawn:\n"
                + "  default_format:\n"
                + "    - \"everyone\"\n"
                + "  groups:\n"
                + "    vip:\n"
                + "      - \"vip line\"\n");
        assertEquals(Collections.singletonList("vip line"),
                YamlConfigAdapter.spawnNametagTemplate(cfg, "vip"));
        assertEquals(Collections.singletonList("everyone"),
                YamlConfigAdapter.spawnNametagTemplate(cfg, "moderator"));
    }
}
