package dev.iiahmed.disguise;

import dev.iiahmed.disguise.vs.*;
import dev.iiahmed.disguise.listener.PlayerListener;
import dev.iiahmed.disguise.packet.DisguiseRegistry;
import dev.iiahmed.disguise.packet.LegacyPacketAccessor;
import dev.iiahmed.disguise.packet.rewriter.PlayerInfoNickRewriter;
import dev.iiahmed.disguise.packet.runtime.ErrorPolicy;
import dev.iiahmed.disguise.packet.runtime.PacketPipeline;
import dev.iiahmed.disguise.packet.runtime.PacketPipelineHandler;
import dev.iiahmed.disguise.packet.runtime.SelfViewFilter;
import dev.iiahmed.disguise.packet.runtime.SimpleRewriteContext;
import dev.iiahmed.disguise.util.Version;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.logging.Level;

@SuppressWarnings("unused")
public final class DisguiseManager {

    private static final DisguiseProvider PROVIDER;
    private static final LegacyPacketAccessor PACKET_ACCESSOR;
    private static final DisguiseRegistry PACKET_REGISTRY;
    private static final PacketPipeline PACKET_PIPELINE;

    static {
        switch (Version.NMS) {
            case "1_8_R3":
                PROVIDER = new VS1_8_R3();
                break;
            case "1_9_R2":
                PROVIDER = new VS1_9_R2();
                break;
            case "1_10_R1":
                PROVIDER = new VS1_10_R1();
                break;
            case "1_11_R1":
                PROVIDER = new VS1_11_R1();
                break;
            case "1_12_R1":
                PROVIDER = new VS1_12_R1();
                break;
            case "1_13_R1":
                PROVIDER = new VS1_13_R1();
                break;
            case "1_13_R2":
                PROVIDER = new VS1_13_R2();
                break;
            case "1_14_R1":
                PROVIDER = new VS1_14_R1();
                break;
            case "1_15_R1":
                PROVIDER = new VS1_15_R1();
                break;
            case "1_16_R1":
                PROVIDER = new VS1_16_R1();
                break;
            case "1_16_R2":
                PROVIDER = new VS1_16_R2();
                break;
            case "1_16_R3":
                PROVIDER = new VS1_16_R3();
                break;
            case "1_17_R1":
                PROVIDER = new VS1_17_R1();
                break;
            case "1_18_R1":
                PROVIDER = new VS1_18_R1();
                break;
            case "1_18_R2":
                PROVIDER = new VS1_18_R2();
                break;
            case "1_19_R1":
                PROVIDER = new VS1_19_R1();
                break;
            case "1_19_R2":
                PROVIDER = new VS1_19_R2();
                break;
            case "1_19_R3":
                PROVIDER = new VS1_19_R3();
                break;
            case "1_20_R1":
                PROVIDER = new VS1_20_R1();
                break;
            case "1_20_R2":
                PROVIDER = new VS1_20_R2();
                break;
            case "1_20_R3":
                PROVIDER = new VS1_20_R3();
                break;
            case "1_20_R4":
                PROVIDER = Version.IS_PAPER ? new PVS1_20_R4() : new SVS1_20_R4();
                break;
            case "1_21_R1":
                PROVIDER = Version.IS_PAPER ? new PVS1_21_R1() : new SVS1_21_R1();
                break;
            case "1_21_R2":
                PROVIDER = Version.IS_PAPER ? new PVS1_21_R2() : new SVS1_21_R2();
                break;
            case "1_21_R3":
                PROVIDER = Version.IS_PAPER ? new PVS1_21_R3() : new SVS1_21_R3();
                break;
            case "1_21_R4":
                PROVIDER = Version.IS_PAPER ? new PVS1_21_R4() : new SVS1_21_R4();
                break;
            case "1_21_R5":
                PROVIDER = Version.IS_PAPER ? new PVS1_21_R5() : new SVS1_21_R5();
                break;
            case "1_21_R6":
                PROVIDER = Version.IS_PAPER ? new PVS1_21_R6() : new SVS1_21_R6();
                break;
            case "1_21_R7":
                PROVIDER = Version.IS_PAPER ? new PVS1_21_R7() : new SVS1_21_R7();
                break;
            case "UNKNOWN":
            default:
                PROVIDER = new VS_Unavailable();
                break;
        }

        // Build the packet-rewriting pipeline for legacy NMS versions
        // (1.8 – 1.16). 1.17+ uses a different NMS namespace and will
        // get its own accessor implementation later.
        LegacyPacketAccessor accessor = null;
        DisguiseRegistry registry = null;
        PacketPipeline pipeline = null;
        if (Version.isBelow(17) && !"UNKNOWN".equals(Version.NMS)) {
            try {
                accessor = new LegacyPacketAccessor("v" + Version.NMS);
                registry = PROVIDER::getActiveInfo;
                pipeline = new PacketPipeline(
                        Collections.singletonList(new PlayerInfoNickRewriter()),
                        SelfViewFilter.ALWAYS,
                        ErrorPolicy.logOnce(Bukkit.getLogger())
                );
            } catch (final Throwable t) {
                Bukkit.getLogger().log(Level.WARNING,
                        "[ModernDisguise] Failed to initialize packet-rewriting pipeline; "
                                + "falling back to GameProfile mutation only.", t);
                accessor = null;
                registry = null;
                pipeline = null;
            }
        }
        PACKET_ACCESSOR = accessor;
        PACKET_REGISTRY = registry;
        PACKET_PIPELINE = pipeline;
    }

    /**
     * Sets the plugin for the provider and registers the litsners
     */
    public static void initialize(@NotNull final Plugin plugin, final boolean entityDisguises) {
        final Plugin old = PROVIDER.getPlugin();
        if (old == null || !old.isEnabled()) {
            PROVIDER.plugin = plugin;
            PROVIDER.entityDisguises = entityDisguises;
            plugin.getServer().getPluginManager().registerEvents(new PlayerListener(), plugin);
        }
    }

    /**
     * @return the available DisguiseProvider for current version
     */
    @NotNull
    public static DisguiseProvider getProvider() {
        return PROVIDER;
    }

    /**
     * Build a per-player {@link PacketPipelineHandler} ready to be
     * installed into the player's netty channel, or {@code null} if
     * the packet-rewriting pipeline is not available on this server
     * version.
     *
     * <p>Called from {@code PlayerJoinEvent} once per player.</p>
     */
    public static @Nullable PacketPipelineHandler newPipelineHandler(@NotNull final Player player) {
        if (PACKET_PIPELINE == null) {
            return null;
        }
        return new PacketPipelineHandler(
                PACKET_PIPELINE,
                new SimpleRewriteContext(player, PACKET_ACCESSOR, PACKET_REGISTRY)
        );
    }

}
