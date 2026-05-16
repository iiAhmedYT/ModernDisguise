package dev.iiahmed.disguise.packet.runtime;

import dev.iiahmed.disguise.packet.LogicalPacket;
import dev.iiahmed.disguise.packet.RewriteResult;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

/**
 * Netty {@link ChannelDuplexHandler} that funnels outbound packets
 * through a {@link PacketPipeline}.
 *
 * <p>One handler per player. The handler runs on the channel's event
 * loop thread, so no internal synchronization is required.</p>
 *
 * <p>Suggested installation (after the player's channel has been
 * obtained by the version module):</p>
 * <pre>{@code
 * channel.pipeline().addBefore(
 *     "packet_handler",
 *     PacketPipelineHandler.HANDLER_NAME,
 *     handler);
 * }</pre>
 * Install before the encoder so the pipeline sees decoded packet
 * objects rather than raw {@code ByteBuf}s.
 */
public final class PacketPipelineHandler extends ChannelDuplexHandler {

    /** Suggested name when installing this handler into a channel pipeline. */
    public static final String HANDLER_NAME = "ModernDisguise-Pipeline";

    private final PacketPipeline pipeline;
    private final SimpleRewriteContext context;

    public PacketPipelineHandler(@NotNull final PacketPipeline pipeline,
                                 @NotNull final SimpleRewriteContext context) {
        this.pipeline = Objects.requireNonNull(pipeline, "pipeline");
        this.context = Objects.requireNonNull(context, "context");
    }

    @Override
    public void write(final ChannelHandlerContext ctx,
                      final Object msg,
                      final ChannelPromise promise) throws Exception {
        if (msg == null) {
            super.write(ctx, msg, promise);
            return;
        }
        final LogicalPacket type = context.accessor().identify(msg);
        if (type == null) {
            super.write(ctx, msg, promise);
            return;
        }

        final RewriteResult result = (type == LogicalPacket.BUNDLE)
                ? pipeline.dispatchBundle(msg, context)
                : pipeline.dispatch(type, msg, context);

        switch (result.kind()) {
            case PASS:
                super.write(ctx, msg, promise);
                return;
            case REPLACE:
                super.write(ctx, result.replacement(), promise);
                return;
            case DROP:
                // The packet is consumed; complete the promise so any
                // caller awaiting the write future does not hang.
                promise.setSuccess();
                return;
            case EXPAND:
                final List<Object> packets = result.expansion();
                if (packets.isEmpty()) {
                    promise.setSuccess();
                    return;
                }
                final int last = packets.size() - 1;
                for (int i = 0; i < last; i++) {
                    super.write(ctx, packets.get(i), ctx.newPromise());
                }
                super.write(ctx, packets.get(last), promise);
                return;
        }
    }
}
