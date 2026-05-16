package dev.iiahmed.disguise.packet.runtime;

import dev.iiahmed.disguise.packet.LogicalPacket;
import dev.iiahmed.disguise.packet.PacketAccessor;
import dev.iiahmed.disguise.packet.PacketRewriter;
import dev.iiahmed.disguise.packet.RewriteContext;
import dev.iiahmed.disguise.packet.RewritePhase;
import dev.iiahmed.disguise.packet.RewriteResult;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Version-independent dispatcher: walks the {@link PacketRewriter}
 * chain for a single packet, honours each rewriter's
 * {@link RewriteResult}, and returns one aggregate result.
 *
 * <p>Built once per server. Registration happens at construction time
 * and the internal structures are frozen afterwards, so the hot path
 * (dispatch on a Netty I/O thread) is lock-free.</p>
 *
 * <p>Phase ordering follows {@link RewritePhase#values()}: every
 * rewriter in an earlier phase runs before any rewriter in a later
 * phase. Within a phase the order between rewriters is undefined;
 * rewriters sharing a phase must be independent.</p>
 *
 * <p>Fail-soft: if a rewriter throws, the configured {@link ErrorPolicy}
 * is notified and the chain continues with the packet unchanged by
 * that rewriter. The Netty channel is never broken by a throwing
 * rewriter.</p>
 */
public final class PacketPipeline {

    private final Map<LogicalPacket, Map<RewritePhase, List<PacketRewriter>>> chains;
    private final SelfViewFilter selfView;
    private final ErrorPolicy errorPolicy;

    /**
     * @param rewriters   all rewriters to install; order within a phase
     *                    is not guaranteed
     * @param selfView    predicate consulted before each rewriter call
     * @param errorPolicy notified when a rewriter throws
     */
    public PacketPipeline(@NotNull final Collection<? extends PacketRewriter> rewriters,
                          @NotNull final SelfViewFilter selfView,
                          @NotNull final ErrorPolicy errorPolicy) {
        Objects.requireNonNull(rewriters, "rewriters");
        this.selfView = Objects.requireNonNull(selfView, "selfView");
        this.errorPolicy = Objects.requireNonNull(errorPolicy, "errorPolicy");

        final Map<LogicalPacket, Map<RewritePhase, List<PacketRewriter>>> mutable = new HashMap<>();
        for (final PacketRewriter rewriter : rewriters) {
            Objects.requireNonNull(rewriter, "rewriter");
            mutable.computeIfAbsent(rewriter.type(), k -> new EnumMap<>(RewritePhase.class))
                    .computeIfAbsent(rewriter.phase(), k -> new ArrayList<>())
                    .add(rewriter);
        }

        final Map<LogicalPacket, Map<RewritePhase, List<PacketRewriter>>> frozen = new HashMap<>(mutable.size());
        for (final Map.Entry<LogicalPacket, Map<RewritePhase, List<PacketRewriter>>> e : mutable.entrySet()) {
            final EnumMap<RewritePhase, List<PacketRewriter>> phases = new EnumMap<>(RewritePhase.class);
            for (final Map.Entry<RewritePhase, List<PacketRewriter>> p : e.getValue().entrySet()) {
                phases.put(p.getKey(), Collections.unmodifiableList(new ArrayList<>(p.getValue())));
            }
            frozen.put(e.getKey(), Collections.unmodifiableMap(phases));
        }
        this.chains = Collections.unmodifiableMap(frozen);
    }

    /** Convenience: {@link SelfViewFilter#ALWAYS} + {@link ErrorPolicy#ignore()}. */
    public PacketPipeline(@NotNull final Collection<? extends PacketRewriter> rewriters) {
        this(rewriters, SelfViewFilter.ALWAYS, ErrorPolicy.ignore());
    }

    /**
     * Run the rewriter chain for a single non-bundle logical packet.
     *
     * @param type    the logical packet type; must not be
     *                {@link LogicalPacket#BUNDLE} (use
     *                {@link #dispatchBundle} for those)
     * @param packet  the NMS packet to rewrite
     * @param context the per-connection rewrite context
     * @return the aggregate result: {@link RewriteResult#pass()} if no
     *         rewriter modified the packet, otherwise the final
     *         {@code REPLACE}, {@code EXPAND}, or {@code DROP}
     */
    public @NotNull RewriteResult dispatch(@NotNull final LogicalPacket type,
                                           @NotNull final Object packet,
                                           @NotNull final RewriteContext context) {
        if (type == LogicalPacket.BUNDLE) {
            throw new IllegalArgumentException("Use dispatchBundle for BUNDLE packets");
        }
        final Map<RewritePhase, List<PacketRewriter>> phaseMap = chains.get(type);
        if (phaseMap == null) {
            return RewriteResult.pass();
        }

        Object current = packet;
        boolean modified = false;
        for (final RewritePhase phase : RewritePhase.values()) {
            final List<PacketRewriter> rewriters = phaseMap.get(phase);
            if (rewriters == null) continue;
            for (int i = 0, n = rewriters.size(); i < n; i++) {
                final PacketRewriter rewriter = rewriters.get(i);
                if (!selfView.shouldRun(rewriter, context)) continue;

                final RewriteResult result;
                try {
                    result = rewriter.rewrite(current, context);
                } catch (final Throwable t) {
                    safeOnException(t, rewriter, type);
                    continue;
                }
                if (result == null) {
                    continue;
                }
                switch (result.kind()) {
                    case PASS:
                        break;
                    case REPLACE:
                        current = Objects.requireNonNull(result.replacement(),
                                "REPLACE result with null replacement");
                        modified = true;
                        break;
                    case DROP:
                        return RewriteResult.drop();
                    case EXPAND:
                        return result;
                }
            }
        }
        return modified ? RewriteResult.replace(current) : RewriteResult.pass();
    }

    /**
     * Unwrap a bundle, dispatch each contained packet, and re-wrap the
     * result. {@code DROP} results remove the part from the bundle;
     * {@code EXPAND} results splice in their packets in order. If every
     * part is dropped, the bundle itself is dropped.
     *
     * <p>Nested bundles are passed through opaquely — the protocol does
     * not produce them, so the pipeline does not recurse.</p>
     *
     * @param bundle  the bundle packet
     * @param context the per-connection rewrite context
     * @return the aggregate result for the bundle as a whole
     */
    public @NotNull RewriteResult dispatchBundle(@NotNull final Object bundle,
                                                 @NotNull final RewriteContext context) {
        final PacketAccessor accessor = context.accessor();
        if (!accessor.supportsBundles()) {
            return RewriteResult.pass();
        }

        final List<Object> parts = accessor.unbundle(bundle);
        final List<Object> rewritten = new ArrayList<>(parts.size());
        boolean modified = false;

        for (int i = 0, n = parts.size(); i < n; i++) {
            final Object part = parts.get(i);
            final LogicalPacket partType = accessor.identify(part);
            if (partType == null || partType == LogicalPacket.BUNDLE) {
                rewritten.add(part);
                continue;
            }
            final RewriteResult result = dispatch(partType, part, context);
            switch (result.kind()) {
                case PASS:
                    rewritten.add(part);
                    break;
                case REPLACE:
                    rewritten.add(result.replacement());
                    modified = true;
                    break;
                case EXPAND:
                    rewritten.addAll(result.expansion());
                    modified = true;
                    break;
                case DROP:
                    modified = true;
                    break;
            }
        }
        if (rewritten.isEmpty()) {
            return RewriteResult.drop();
        }
        if (!modified) {
            return RewriteResult.pass();
        }
        return RewriteResult.replace(accessor.bundle(rewritten));
    }

    private void safeOnException(final Throwable t, final PacketRewriter rewriter, final LogicalPacket type) {
        try {
            errorPolicy.onException(t, rewriter, type);
        } catch (final Throwable ignored) {
            // A broken error policy must not poison the pipeline.
        }
    }
}
