package dev.iiahmed.disguise.packet.rewriter;

import dev.iiahmed.disguise.PlayerInfo;
import dev.iiahmed.disguise.packet.ChatView;
import dev.iiahmed.disguise.packet.LogicalPacket;
import dev.iiahmed.disguise.packet.PacketRewriter;
import dev.iiahmed.disguise.packet.RewriteContext;
import dev.iiahmed.disguise.packet.RewritePhase;
import dev.iiahmed.disguise.packet.RewriteResult;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Rewrites chat packets so that real player names of disguised players
 * are replaced with their nicknames in the displayed message.
 *
 * <p>Plugins format chat by reading {@code player.getName()}; if the
 * server keeps the real name in its internal state, the resulting
 * outbound chat packet carries the real name. This rewriter rewrites
 * the chat content's JSON serialization, substituting each disguised
 * player's real name with their nickname.</p>
 *
 * <p>Substitution uses word-boundary regex
 * ({@code \b<name>\b}) so a real name that is a prefix of another
 * token (or appears inside a longer word) is not replaced. Minecraft
 * player names are restricted to {@code [A-Za-z0-9_]} which Java's
 * regex word class matches exactly, so {@link Pattern#quote} is
 * unnecessary on the body and {@code \b} aligns with the lexical
 * boundary of the name.</p>
 *
 * <p>Runs in {@link RewritePhase#RELATIONS}.</p>
 */
public final class ChatNickRewriter implements PacketRewriter {

    @Override
    public @NotNull LogicalPacket type() {
        return LogicalPacket.SYSTEM_CHAT;
    }

    @Override
    public @NotNull RewritePhase phase() {
        return RewritePhase.RELATIONS;
    }

    @Override
    public @NotNull RewriteResult rewrite(@NotNull final Object packet,
                                          @NotNull final RewriteContext context) {
        final Collection<PlayerInfo> disguises = context.registry().activeDisguises();
        if (disguises.isEmpty()) {
            return RewriteResult.pass();
        }

        final ChatView view = context.accessor().chatView(packet);
        final String original = view.json();
        if (original == null || original.isEmpty()) {
            return RewriteResult.pass();
        }

        String rewritten = original;
        for (final PlayerInfo info : disguises) {
            if (!info.hasName()) continue;
            final String real = info.getName();
            // Pre-check to avoid building a Pattern for names that
            // aren't even mentioned; the regex pass is the expensive
            // part on hot chat servers.
            if (rewritten.indexOf(real) < 0) continue;
            rewritten = wordBoundaryReplace(rewritten, real, info.getNickname());
        }
        if (rewritten.equals(original)) {
            return RewriteResult.pass();
        }
        view.setJson(rewritten);
        return RewriteResult.replace(packet);
    }

    private static @NotNull String wordBoundaryReplace(@NotNull final String input,
                                                       @NotNull final String name,
                                                       @NotNull final String replacement) {
        final Pattern pattern = Pattern.compile("\\b" + name + "\\b");
        return pattern.matcher(input).replaceAll(Matcher.quoteReplacement(replacement));
    }
}
