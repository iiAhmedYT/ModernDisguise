package dev.iiahmed.disguise.packet;

/**
 * Execution phases for {@link PacketRewriter}s.
 *
 * <p>The pipeline runs all rewriters in {@link #IDENTITY} first, then
 * {@link #APPEARANCE}, then {@link #RELATIONS}, then {@link #STRUCTURAL}.
 * Within a single phase the order between rewriters is undefined, so
 * rewriters sharing a phase must be independent of one another.</p>
 *
 * <p>Phases exist instead of numeric priorities so coupling between
 * rewriters is documented by which phase a rewriter is placed in, not
 * by an opaque integer.</p>
 */
public enum RewritePhase {

    /**
     * Establishes "who" the packet is about: name, skin, and UUID
     * substitutions on profile-bearing packets.
     */
    IDENTITY,

    /**
     * Establishes "what" the disguise looks like: entity type swaps,
     * entity metadata translation, equipment.
     */
    APPEARANCE,

    /**
     * Rewrites references to identity established in earlier phases:
     * scoreboard teams, objectives, scores, chat sender names, command
     * suggestion lists.
     */
    RELATIONS,

    /**
     * Final structural fix-ups: bundle composition, packet ordering,
     * anything that depends on the post-rewrite shape of the stream.
     */
    STRUCTURAL
}
