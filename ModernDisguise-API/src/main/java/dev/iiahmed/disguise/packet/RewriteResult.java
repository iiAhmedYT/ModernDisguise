package dev.iiahmed.disguise.packet;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * The outcome of a single {@link PacketRewriter#rewrite} invocation.
 *
 * <p>Construct via the static factories ({@link #pass()},
 * {@link #replace(Object)}, {@link #expand(List)}, {@link #drop()}) and
 * dispatch on {@link #kind()}.</p>
 *
 * <p>Instances are immutable; {@link #pass()} and {@link #drop()} are
 * cached singletons.</p>
 */
public final class RewriteResult {

    /** Action requested by the rewriter. */
    public enum Kind {

        /** Leave the packet unchanged; continue the rewriter chain. */
        PASS,

        /** Replace the packet with a new instance and continue the chain. */
        REPLACE,

        /**
         * Replace the packet with multiple packets, in order. The
         * rewriter chain terminates for the original packet after this
         * result is returned.
         */
        EXPAND,

        /** Drop the packet entirely; no further rewriters run. */
        DROP
    }

    private static final RewriteResult PASS =
            new RewriteResult(Kind.PASS, null, Collections.emptyList());
    private static final RewriteResult DROP =
            new RewriteResult(Kind.DROP, null, Collections.emptyList());

    private final Kind kind;
    private final Object replacement;
    private final List<Object> expansion;

    private RewriteResult(final Kind kind,
                          final Object replacement,
                          final List<Object> expansion) {
        this.kind = kind;
        this.replacement = replacement;
        this.expansion = expansion;
    }

    /** Pass-through: the packet was not modified. */
    public static @NotNull RewriteResult pass() {
        return PASS;
    }

    /** Replace the packet with {@code replacement}. */
    public static @NotNull RewriteResult replace(@NotNull final Object replacement) {
        return new RewriteResult(Kind.REPLACE, Objects.requireNonNull(replacement), Collections.emptyList());
    }

    /** Expand the packet into multiple packets, in order. */
    public static @NotNull RewriteResult expand(@NotNull final List<Object> packets) {
        return new RewriteResult(Kind.EXPAND, null,
                Collections.unmodifiableList(Objects.requireNonNull(packets)));
    }

    /** Drop the packet entirely. */
    public static @NotNull RewriteResult drop() {
        return DROP;
    }

    /** @return the action this result describes */
    public @NotNull Kind kind() {
        return kind;
    }

    /**
     * @return the replacement packet, if {@link #kind()} is
     *         {@link Kind#REPLACE}; {@code null} otherwise
     */
    public @Nullable Object replacement() {
        return replacement;
    }

    /**
     * @return the expansion packets, if {@link #kind()} is
     *         {@link Kind#EXPAND}; an empty list otherwise
     */
    public @NotNull List<Object> expansion() {
        return expansion;
    }
}
