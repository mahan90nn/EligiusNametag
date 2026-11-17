package com.makrozai.eligiusnametag.adapter.platform;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Exercises the object-component fallback path of TagParser. On the plain JVM test
 * classpath the probe resolves ObjectContents from the plugin's own classloader
 * (same loader as TagParser), which the parser treats as "unsupported" — exactly
 * the behaviour old Paper/Spigot servers get: tags are stripped, text/colors survive.
 */
public class TagParserTest {

    @Test
    public void testBareHeadTagIsStrippedWhenUnsupported() {
        Component c = TagParser.parse("Hello <head>!", "Notch");
        String plain = plainText(c);
        assertFalse(plain.contains("head"));
        assertFalse(plain.contains("Notch"));
    }

    @Test
    public void testParameterizedHeadAndSpriteTagsAreStrippedWhenUnsupported() {
        Component c = TagParser.parse("<head:Steve> <sprite:blocks:block/stone> Hi", null);
        String plain = plainText(c);
        assertFalse(plain.contains("head"));
        assertFalse(plain.contains("sprite"));
        assertTrue(plain.contains("Hi"));
    }

    @Test
    public void testRegularTextAndColorsSurvive() {
        Component c = TagParser.parse("<red>Admin</red> <bold><PLAYER></bold>", null);
        String plain = plainText(c);
        assertEquals("Admin <PLAYER>", plain.trim());
        assertTrue(c.color() == NamedTextColor.RED || !c.children().isEmpty());
    }

    @Test
    public void testBase64HeadTagIsStrippedWhenUnsupported() {
        String b64 = java.util.Base64.getEncoder().encodeToString(
                "{\"textures\":{\"SKIN\":{\"url\":\"http://x\"}}}".getBytes(java.nio.charset.StandardCharsets.UTF_8));
        assertTrue(b64.startsWith("eyJ"));
        Component c = TagParser.parse("A <head:" + b64 + ":false> B", "Notch");
        String plain = plainText(c);
        assertFalse(plain.contains("head"));
        assertFalse(plain.contains("eyJ"));
        assertTrue(plain.contains("A"));
        assertTrue(plain.contains("B"));
    }

    @Test
    public void testTextureValueClassification() {
        String b64 = java.util.Base64.getEncoder().encodeToString(
                "{\"textures\":{\"SKIN\":{\"url\":\"http://textures.minecraft.net/texture/aa\"}}}"
                        .getBytes(java.nio.charset.StandardCharsets.UTF_8));
        assertEquals(TagParser.TextureValue.BASE64, TagParser.classifyTextureValue(b64));

        String hash = "3b8d2f5414d1c6b4a0e7f9c8d1e2f3a4b5c6d7e8f9a0b1c2d3e4f5a6b7c8d9e0";
        assertEquals(TagParser.TextureValue.HASH, TagParser.classifyTextureValue(hash));

        assertEquals(null, TagParser.classifyTextureValue("Notch"));
        assertEquals(null, TagParser.classifyTextureValue("069a79f4-44e9-4726-a5be-fca90e38aaf5"));
        assertEquals(null, TagParser.classifyTextureValue("entity/player/wide/steve"));
        assertEquals(null, TagParser.classifyTextureValue(java.util.Base64.getEncoder()
                .encodeToString("some random binary".getBytes(java.nio.charset.StandardCharsets.UTF_8))));
    }

    private static String plainText(Component component) {
        StringBuilder sb = new StringBuilder();
        appendPlain(component, sb);
        return sb.toString();
    }

    private static void appendPlain(Component component, StringBuilder sb) {
        if (component instanceof net.kyori.adventure.text.TextComponent text) {
            sb.append(text.content());
        }
        for (Component child : component.children()) {
            appendPlain(child, sb);
        }
    }
}
