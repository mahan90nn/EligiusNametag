package com.makrozai.eligiusnametag.adapter.platform;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * Parses nametag template strings into Adventure components, adding support for the
 * 1.21.9 "object" text components: {@code <head>} (player heads) and
 * {@code <sprite:atlas:sprite>} (atlas sprites).
 *
 * <p>Both tags are native MiniMessage tags since adventure 4.25.0 (shipped by Paper
 * 1.21.9+), so this parser only handles two things:</p>
 * <ul>
 *   <li>a bare {@code <head>} tag, which MiniMessage cannot resolve on its own — it is
 *       rewritten to {@code <head:PlayerName>} using the nametag's own target
 *       (the player, or the pet's owner);</li>
 *   <li>the graceful fallback for runtimes whose server-side Adventure does not know
 *       these tags (Paper &lt; 1.21.9, Spigot): the tags are stripped instead of being
 *       left as literal text, so nothing breaks.</li>
 * </ul>
 */
public final class TagParser {
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final Pattern HEAD_TAG = Pattern.compile("<head(:[^>]*)?>");
    private static final Pattern SPRITE_TAG = Pattern.compile("<sprite:[^>]*>");
    private static final String UNSUPPORTED_WARNING =
            "Object components (<head>, <sprite>) need Paper 1.21.9+ / adventure 4.25+. "
                    + "Those tags will be stripped from nametags on this server.";

    private static Boolean objectSupport;
    private static boolean warnedUnsupported = false;

    private TagParser() {}

    /**
     * True when the runtime's own Adventure — the one the server uses to serialize
     * TextDisplay content — knows object components (adventure >= 4.25.0, which Paper
     * ships since 1.21.9). The copy shaded into this jar for Spigot must NOT be used:
     * its components would be handed to the server's older serializer, so a class
     * resolved from the plugin's own classloader counts as "not supported".
     */
    public static synchronized boolean supportsObjectComponents() {
        if (objectSupport == null) {
            try {
                Class<?> cls = Class.forName("net.kyori.adventure.text.object.ObjectContents");
                objectSupport = cls.getClassLoader() != TagParser.class.getClassLoader();
            } catch (ClassNotFoundException | NoClassDefFoundError e) {
                objectSupport = false;
            }
        }
        return objectSupport;
    }

    /**
     * Deserializes a raw template string with MiniMessage.
     *
     * @param raw          the template after placeholder/animation/legacy conversion
     * @param selfHeadName name used for a bare {@code <head>} tag (the nametag's own
     *                     player, or the pet's owner); null removes bare {@code <head>}
     * @return the parsed component
     */
    public static Component parse(String raw, String selfHeadName) {
        if (selfHeadName != null && !selfHeadName.isEmpty()) {
            raw = raw.replace("<head>", "<head:" + selfHeadName + ">");
        }
        if (!supportsObjectComponents()) {
            if (HEAD_TAG.matcher(raw).find() || SPRITE_TAG.matcher(raw).find()) {
                raw = HEAD_TAG.matcher(raw).replaceAll("");
                raw = SPRITE_TAG.matcher(raw).replaceAll("");
                warnOnce();
            }
        }
        return MINI_MESSAGE.deserialize(raw);
    }

    private static void warnOnce() {
        if (!warnedUnsupported) {
            warnedUnsupported = true;
            Logger.getLogger("EligiusNametag").warning(UNSUPPORTED_WARNING);
        }
    }
}
