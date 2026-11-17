package com.makrozai.eligiusnametag.adapter.platform;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.object.ObjectContents;
import net.kyori.adventure.text.object.PlayerHeadObjectContents;

/**
 * Builds head components from raw texture values (base64 texture property or a
 * textures.minecraft.net hash). This class carries static references to the
 * object-component API, so it must ONLY be loaded when the runtime's
 * server-provided Adventure knows object components — TagParser probes for that
 * (adventure 4.25+, Paper 1.21.9+) before touching it. On Spigot / pre-1.21.9
 * Paper the shaded Adventure copy would shadow the server's serializer types and
 * produce components the server cannot serialize.
 */
final class ObjectHeads {
    private ObjectHeads() {}

    /**
     * @param textureValue raw textures property value (base64 JSON as used by
     *                     /give player_head "Value" fields)
     * @param hat          whether the hat (outer) layer renders
     * @return the head component, or null when the runtime rejects the build
     */
    static Component headFromTexture(String textureValue, boolean hat) {
        try {
            return Component.object(ObjectContents.playerHead()
                    .profileProperty(PlayerHeadObjectContents.property("textures", textureValue))
                    .hat(hat)
                    .build());
        } catch (Throwable t) {
            return null;
        }
    }
}
