package com.makrozai.eligiusnametag.adapter.platform;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses nametag template strings into Adventure components, adding support for the
 * 1.21.9 "object" text components: {@code <head>} (player heads) and
 * {@code <sprite:atlas:sprite>} (atlas sprites).
 *
 * <p>Head tags accept the native MiniMessage arguments (player name, UUID or a
 * texture path) plus raw skin values the stock tag does not know: a base64 texture
 * value (the "Value" field used by /give player_head and head sites) or a 64-char
 * textures.minecraft.net texture hash — {@code <head:eyJ0ZXh0dXJlcyI6...>} /
 * {@code <head:VALUE:false>} for the hat layer. Those values cannot be profile
 * names, so they are detected, replaced by private-use markers and spliced into the
 * MiniMessage result as object components afterwards.</p>
 *
 * <p>On runtimes whose server-side Adventure does not know object components
 * (Paper &lt; 1.21.9, Spigot) all object tags are stripped instead of being left as
 * literal text, so nothing breaks.</p>
 */
public final class TagParser {
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final Pattern HEAD_TAG = Pattern.compile("<head(:[^>]*)?>");
    private static final Pattern SPRITE_TAG = Pattern.compile("<sprite:[^>]*>");
    private static final String UNSUPPORTED_WARNING =
            "Object components (<head>, <sprite>) need Paper 1.21.9+ / adventure 4.25+. "
                    + "Those tags will be stripped from nametags on this server.";

    /** Base64 texture values / texture hashes are 40+ chars of base64 charset — no player name (16 max) or UUID (36 max with dashes) reaches that. */
    static final Pattern HEAD_VALUE_TAG = Pattern.compile("<head:([A-Za-z0-9+/=_-]{40,})(?::(true|false))?>");
    private static final Pattern HEX_64 = Pattern.compile("[0-9a-fA-F]{64}");
    private static final Pattern MARKERS = Pattern.compile("\uE000(\\d+)\uE001");
    private static final char MARKER_START = '\uE000';
    private static final char MARKER_END = '\uE001';

    /** What a detected head argument is: a raw base64 texture value or a texture hash. */
    enum TextureValue { BASE64, HASH }

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
     * Classifies a {@code <head:VALUE>} argument: a raw base64 texture property
     * value (the {"textures":...} JSON used by head sites and /give), a 64-char
     * textures.minecraft.net hash, or null when it is not a skin value (player
     * name, UUID, texture path — left to MiniMessage's native handling).
     */
    static TextureValue classifyTextureValue(String arg) {
        if (arg.length() == 64 && HEX_64.matcher(arg).matches()) {
            return TextureValue.HASH;
        }
        if (arg.startsWith("eyJ")) {
            return TextureValue.BASE64;
        }
        if (arg.length() >= 100) {
            try {
                if (new String(Base64.getMimeDecoder().decode(arg), StandardCharsets.UTF_8).contains("textures")) {
                    return TextureValue.BASE64;
                }
            } catch (IllegalArgumentException ignored) {
                // not base64 after all — fall through
            }
        }
        return null;
    }

    /**
     * Deserializes a raw template string with MiniMessage.
     *
     * @param raw          the template after placeholder/animation/legacy conversion
     * @param selfHeadName name used for a bare {@code <head>} tag (the nametag's own
     *                     player, or the pet's owner); null leaves bare {@code <head>}
     *                     to MiniMessage (which renders a viewer-style head)
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
            return MINI_MESSAGE.deserialize(raw);
        }

        // Intercept head tags with raw skin values (base64 / hash) that MiniMessage
        // cannot resolve, hide them behind markers, parse everything else normally
        // and splice the head components back in (keeping the surrounding style).
        Map<Integer, String[]> heads = new HashMap<>();
        StringBuilder sb = new StringBuilder();
        Matcher m = HEAD_VALUE_TAG.matcher(raw);
        int index = 0;
        while (m.find()) {
            String arg = m.group(1);
            TextureValue kind = classifyTextureValue(arg);
            if (kind == null) {
                m.appendReplacement(sb, Matcher.quoteReplacement(m.group()));
                continue; // name/uuid/texture path — leave for MiniMessage
            }
            String value = kind == TextureValue.HASH
                    ? Base64.getEncoder().encodeToString(("{\"textures\":{\"SKIN\":{\"url\":\"http://textures.minecraft.net/texture/"
                            + arg.toLowerCase() + "\"}}}").getBytes(StandardCharsets.UTF_8))
                    : arg;
            heads.put(index, new String[] {value, m.group(2) == null ? "true" : m.group(2)});
            m.appendReplacement(sb, Matcher.quoteReplacement(String.valueOf(MARKER_START) + index + MARKER_END));
            index++;
        }
        m.appendTail(sb);
        if (index == 0) {
            return MINI_MESSAGE.deserialize(raw);
        }
        Component parsed = MINI_MESSAGE.deserialize(sb.toString());
        return spliceHeads(parsed, heads);
    }

    /** Replaces every marker text node with the matching head component. */
    private static Component spliceHeads(Component component, Map<Integer, String[]> heads) {
        if (component instanceof TextComponent text && text.content().indexOf(MARKER_START) >= 0) {
            List<Component> parts = new ArrayList<>();
            Matcher m = MARKERS.matcher(text.content());
            int last = 0;
            while (m.find()) {
                if (m.start() > last) {
                    parts.add(Component.text(text.content().substring(last, m.start())).style(text.style()));
                }
                String[] spec = heads.get(Integer.parseInt(m.group(1)));
                if (spec != null) {
                    Component head = ObjectHeads.headFromTexture(spec[0], Boolean.parseBoolean(spec[1]));
                    if (head != null) {
                        parts.add(head.style(text.style()));
                    }
                }
                last = m.end();
            }
            if (last < text.content().length()) {
                parts.add(Component.text(text.content().substring(last)).style(text.style()));
            }
            // a marker-bearing node can also carry children (MiniMessage nests tags
            // after the text) — keep them, spliced, after the content
            for (Component child : component.children()) {
                parts.add(spliceHeads(child, heads));
            }
            if (parts.isEmpty()) {
                return Component.empty().style(text.style());
            }
            if (parts.size() == 1) {
                return parts.get(0);
            }
            return Component.empty().style(text.style()).children(parts);
        }
        List<Component> children = component.children();
        if (children.isEmpty()) {
            return component;
        }
        List<Component> unwrapped = new ArrayList<>(children.size());
        boolean changed = false;
        for (Component child : children) {
            Component result = spliceHeads(child, heads);
            changed |= result != child;
            unwrapped.add(result);
        }
        return changed ? component.children(unwrapped) : component;
    }

    private static void warnOnce() {
        if (!warnedUnsupported) {
            warnedUnsupported = true;
            Logger.getLogger("EligiusNametag").warning(UNSUPPORTED_WARNING);
        }
    }
}
