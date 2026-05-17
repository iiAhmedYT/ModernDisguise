package dev.iiahmed.disguise.packet;

import org.jetbrains.annotations.NotNull;

/**
 * Typed view over chat-carrying packets (1.8 – 1.18
 * {@code PacketPlayOutChat}, 1.19+ {@code ClientboundSystemChatPacket}
 * and friends).
 *
 * <p>The chat content is exposed as its JSON serialization rather
 * than a typed component tree. The component classes
 * ({@code IChatBaseComponent} and its impls) are version-specific
 * NMS types; abstracting them as a stable Java API is a substantial
 * project of its own. The JSON form is a stable wire representation
 * the rewriter can manipulate with plain string operations.</p>
 *
 * <p>The view caches nothing — {@link #json()} performs a serialize
 * on each call, and {@link #setJson(String)} performs a deserialize.
 * Rewriters that read the JSON, modify it, and write it back pay the
 * round-trip cost once per packet.</p>
 */
public interface ChatView {

    /**
     * @return the chat content serialized to its JSON representation,
     *         or {@code null} if the packet carries no component
     *         (rare; usually means the packet is malformed)
     */
    String json();

    /**
     * Replace the chat content from a JSON representation.
     *
     * <p>Mutates the underlying packet on legacy versions; a rewriter
     * that calls this must return
     * {@link RewriteResult#replace(Object) replace(packet)}.</p>
     *
     * @param json the new component as a JSON string
     */
    void setJson(@NotNull String json);
}
