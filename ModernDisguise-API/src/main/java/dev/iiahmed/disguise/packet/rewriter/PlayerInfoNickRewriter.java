package dev.iiahmed.disguise.packet.rewriter;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import dev.iiahmed.disguise.PlayerInfo;
import dev.iiahmed.disguise.Skin;
import dev.iiahmed.disguise.packet.LogicalPacket;
import dev.iiahmed.disguise.packet.PacketRewriter;
import dev.iiahmed.disguise.packet.PlayerInfoView;
import dev.iiahmed.disguise.packet.RewriteContext;
import dev.iiahmed.disguise.packet.RewritePhase;
import dev.iiahmed.disguise.packet.RewriteResult;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

/**
 * Rewrites {@link LogicalPacket#PLAYER_INFO_UPDATE} packets so disguised
 * players appear in the tab list under their nickname and disguise
 * skin.
 *
 * <p>For each entry whose {@link GameProfile} UUID matches a disguised
 * player in the {@link dev.iiahmed.disguise.packet.DisguiseRegistry},
 * the entry's profile is replaced with a fake profile carrying:</p>
 * <ul>
 *   <li>the original UUID (preserved — clients identify entries by
 *       UUID, so changing it would break subsequent updates and removes)</li>
 *   <li>the disguise nickname</li>
 *   <li>the disguise skin's {@code textures} / {@code signature}
 *       property if the disguise carries a skin; other properties from
 *       the original profile (e.g. cape) are preserved</li>
 * </ul>
 *
 * <p>Runs in {@link RewritePhase#IDENTITY} so subsequent appearance- /
 * relations- phase rewriters see the disguised identity.</p>
 *
 * <p>This rewriter does not differentiate self-view: a disguised
 * player's own tab entry is rewritten too. Plugins that want
 * self-visible real identity can install a
 * {@link dev.iiahmed.disguise.packet.runtime.SelfViewFilter} that
 * skips this rewriter when {@code context.recipient().getUniqueId()}
 * equals the entry's UUID.</p>
 */
public final class PlayerInfoNickRewriter implements PacketRewriter {

    private static final String TEXTURES_PROPERTY = "textures";

    @Override
    public @NotNull LogicalPacket type() {
        return LogicalPacket.PLAYER_INFO_UPDATE;
    }

    @Override
    public @NotNull RewritePhase phase() {
        return RewritePhase.IDENTITY;
    }

    @Override
    public @NotNull RewriteResult rewrite(@NotNull final Object packet,
                                          @NotNull final RewriteContext context) {
        final PlayerInfoView view = context.accessor().playerInfoView(packet);
        final List<PlayerInfoView.Entry> entries = view.entries();
        if (entries.isEmpty()) {
            return RewriteResult.pass();
        }

        boolean modified = false;
        for (int i = 0, n = entries.size(); i < n; i++) {
            final PlayerInfoView.Entry entry = entries.get(i);
            final GameProfile original = entry.profile();
            final UUID id = original.getId();
            if (id == null) {
                continue;
            }
            final PlayerInfo info = context.registry().getInfo(id);
            if (info == null || !info.hasName()) {
                continue;
            }
            entry.setProfile(fakeProfile(original, info));
            modified = true;
        }
        return modified ? RewriteResult.replace(packet) : RewriteResult.pass();
    }

    private static @NotNull GameProfile fakeProfile(@NotNull final GameProfile original,
                                                    @NotNull final PlayerInfo info) {
        final GameProfile fake = new GameProfile(original.getId(), info.getNickname());
        fake.getProperties().putAll(original.getProperties());
        if (info.hasSkin()) {
            final Skin skin = info.getSkin();
            fake.getProperties().removeAll(TEXTURES_PROPERTY);
            fake.getProperties().put(TEXTURES_PROPERTY,
                    new Property(TEXTURES_PROPERTY, skin.getTextures(), skin.getSignature()));
        }
        return fake;
    }
}
