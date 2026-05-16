package dev.iiahmed.disguise.packet;

/**
 * Logical, version-independent packet identifiers.
 *
 * <p>Concrete NMS packet classes are mapped onto these values by
 * {@link PacketAccessor#identify(Object)}. Rewriters register against a
 * {@code LogicalPacket} rather than a specific NMS class so the same
 * rewriter implementation can serve every supported Minecraft version.</p>
 *
 * <p>Add a constant here when a new packet enters the rewrite surface;
 * the version-specific accessors then learn to identify it.</p>
 */
public enum LogicalPacket {

    /** Add / update / initialize tab list entries (1.19.3+ split). */
    PLAYER_INFO_UPDATE,

    /** Remove tab list entries (1.19.3+ split). */
    PLAYER_INFO_REMOVE,

    /** Spawn an entity (player or otherwise) in the world. */
    ADD_ENTITY,

    /** Synchronize an entity's data values (metadata). */
    SET_ENTITY_DATA,

    /** Create, update, or remove a scoreboard team, or change its members. */
    SET_PLAYER_TEAM,

    /** Create, update, or remove a scoreboard objective. */
    SET_OBJECTIVE,

    /** Set a score entry on a scoreboard objective. */
    SET_SCORE,

    /** Reset a score entry on a scoreboard objective. */
    RESET_SCORE,

    /** Signed player chat message (1.19+). */
    PLAYER_CHAT,

    /** System chat message (1.19.1+). */
    SYSTEM_CHAT,

    /** Server response to a client's tab-completion request. */
    COMMAND_SUGGESTIONS,

    /** Server list ping response containing the player sample. */
    STATUS_RESPONSE,

    /** Bundle envelope grouping multiple packets atomically (1.20.2+). */
    BUNDLE
}
