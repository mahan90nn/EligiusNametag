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
