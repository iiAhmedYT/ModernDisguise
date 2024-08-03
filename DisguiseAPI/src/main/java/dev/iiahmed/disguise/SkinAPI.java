package dev.iiahmed.disguise;

import dev.iiahmed.disguise.exception.IDNotFoundException;
import dev.iiahmed.disguise.util.DisguiseUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

@SuppressWarnings("unused")
public class SkinAPI {

    private final Function<Context, Skin> provider;

    public SkinAPI(final Function<Context, Skin> provider) {
        this.provider = provider;
    }

    public Skin of(@NotNull final String name) {
        return provider.apply(new NameContext(name));
    }

    public Skin of(@NotNull final UUID id) {
        return provider.apply(new IDContext(id));
    }

    public static SkinAPI MOJANG = new SkinAPI(context -> {
        final String id = context.getID().orElseThrow(() -> new IDNotFoundException(context.getName().orElse(null))).toString();
        final String url = "https://sessionserver.mojang.com/session/minecraft/profile/%uuid%?unsigned=false".replace("%uuid%", id);
        final JSONObject object = DisguiseUtil.getJSONObject(url);
        return extractSkinFromJSON(object);
    });

    public static SkinAPI MINETOOLS = new SkinAPI(context -> {
        final String id = context.getID().orElseThrow(() -> new IDNotFoundException(context.getName().orElse(null))).toString();
        final String url = "https://api.minetools.eu/profile/%uuid%".replace("%uuid%", id);
        final JSONObject object = DisguiseUtil.getJSONObject(url);
        return extractSkinFromJSON((JSONObject) object.get("raw"));
    });

    private static Skin extractSkinFromJSON(final JSONObject object) {
        String texture = "", signature = "";
        final JSONArray array = (JSONArray) object.get("properties");
        for (final Object o : array) {
            final JSONObject jsonObject = (JSONObject) o;
            if (jsonObject == null) continue;

            texture = (String) jsonObject.get("value");
            signature = (String) jsonObject.get("signature");
        }
        return new Skin(texture, signature);
    }

    public static SkinAPI MINESKIN = new SkinAPI(context -> {
        final String id = context.getID().orElseThrow(() -> new IDNotFoundException(context.getName().orElse(null))).toString();
        final String url = "https://api.mineskin.org/get/uuid/%uuid%".replace("%uuid%", id);
        final JSONObject object = DisguiseUtil.getJSONObject(url);
        final JSONObject dataObject = (JSONObject) object.get("data");
        final JSONObject texturesObject = (JSONObject) dataObject.get("texture");
        return new Skin((String) texturesObject.get("value"), (String) texturesObject.get("signature"));
    });

    private static final Map<String, SkinAPI> SKIN_API_MAP = new HashMap<String, SkinAPI>() {
        {
            put("MOJANG", MOJANG);
            put("MINETOOLS", MINETOOLS);
            put("MINESKIN", MINESKIN);
        }
    };

    public static void register(final String name, final Function<Context, Skin> provider) {
        SKIN_API_MAP.put(name, new SkinAPI(provider));
    }

    public static Optional<SkinAPI> get(final String name) {
        return Optional.ofNullable(SKIN_API_MAP.get(name));
    }

    public enum Requirement {
        NAME, ID
    }

    public interface Context {

        Optional<UUID> getID();
        Optional<String> getName();
        Requirement providedRequirement();

    }

    @ApiStatus.Internal
    static class IDContext implements Context {

        private final UUID id;
        private String name;

        IDContext(final UUID id) {
            this.id = id;
        }

        @Override
        public Optional<UUID> getID() {
            return Optional.of(this.id);
        }

        @Override
        public Optional<String> getName() {
            if (name != null) {
                return Optional.of(name);
            }

            final OfflinePlayer player = Bukkit.getOfflinePlayer(id);
            if (player != null) {
                name = player.getName();
                return Optional.of(name);
            }

            return Optional.empty();
        }

        @Override
        public Requirement providedRequirement() {
            return Requirement.ID;
        }

    }

    @ApiStatus.Internal
    static class NameContext implements Context {

        private final String name;
        private UUID id;

        NameContext(final String name) {
            this.name = name;
        }

        @Override
        @SuppressWarnings("deprecation")
        public Optional<UUID> getID() {
            if (this.id != null) {
                return Optional.of(this.id);
            }

            final OfflinePlayer player = Bukkit.getOfflinePlayer(name);
            if (player != null) {
                this.id = player.getUniqueId();
                return Optional.of(this.id);
            }

            return Optional.empty();
        }

        @Override
        public Optional<String> getName() {
            return Optional.of(this.name);
        }

        @Override
        public Requirement providedRequirement() {
            return Requirement.NAME;
        }

    }

}
