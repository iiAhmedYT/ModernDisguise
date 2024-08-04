package dev.iiahmed.disguise;

import dev.iiahmed.disguise.util.DisguiseUtil;
import org.jetbrains.annotations.NotNull;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.UUID;
import java.util.function.Function;

/**
 * The {@code SkinAPI} class provides a mechanism to fetch Minecraft player skins using different APIs.
 * It allows fetching skins based on a generic context value, such as a UUID.
 *
 * <p>APIs included by default:</p>
 * <ul>
 *     <li>Mojang API</li>
 *     <li>MineTools API</li>
 *     <li>MineSkin API</li>
 * </ul>
 *
 * @param <V> the type of the value used for context, typically {@code UUID}
 */
@SuppressWarnings("unused")
public class SkinAPI<V> {

    private final Function<Context<V>, Skin> provider;

    /**
     * Constructs a new {@code SkinAPI} with the specified provider function.
     *
     * @param provider the function that provides a {@code Skin} given a {@code Context}
     */
    public SkinAPI(final Function<Context<V>, Skin> provider) {
        this.provider = provider;
    }

    /**
     * Fetches a {@code Skin} using the specified value.
     *
     * @param value the value used to identify the context, such as a player's {@code UUID}
     * @return the fetched {@code Skin}
     */
    public Skin of(@NotNull final V value) {
        return provider.apply(() -> value);
    }

    /**
     * Fetches a {@code Skin} using the specified {@code Context}.
     *
     * @param context the context used to provide a value for fetching the skin
     * @return the fetched {@code Skin}
     */
    public Skin of(@NotNull final Context<V> context) {
        return provider.apply(context);
    }

    /**
     * SkinAPI instance for Mojang's skin service.
     * This API fetches skins from Mojang's official session server.
     *
     * @apiNote Requires a Player UUID
     */
    public static final SkinAPI<UUID> MOJANG = new SkinAPI<>(context -> {
        final String id = context.value().toString();
        final String url = "https://sessionserver.mojang.com/session/minecraft/profile/%uuid%?unsigned=false".replace("%uuid%", id);
        final JSONObject object = DisguiseUtil.getJSONObject(url);
        return extractSkinFromJSON(object);
    });

    /**
     * SkinAPI instance for MineTools skin service.
     * This API fetches skins from the MineTools API, a third-party service.
     *
     * @apiNote Requires a Player UUID
     */
    public static final SkinAPI<UUID> MINETOOLS = new SkinAPI<>(context -> {
        final String id = context.value().toString();
        final String url = "https://api.minetools.eu/profile/%uuid%".replace("%uuid%", id);
        final JSONObject object = DisguiseUtil.getJSONObject(url);
        return extractSkinFromJSON((JSONObject) object.get("raw"));
    });

    /**
     * SkinAPI instance for MineSkin skin service.
     * This API fetches skins from the MineSkin API, a third-party service.
     *
     * @apiNote Requires a Skin UUID of their website, example: 808660f5048147aa9846c2e0370e766d
     */
    public static final SkinAPI<UUID> MINESKIN = new SkinAPI<>(context -> {
        final String id = context.value().toString();
        final String url = "https://api.mineskin.org/get/uuid/%uuid%".replace("%uuid%", id);
        final JSONObject object = DisguiseUtil.getJSONObject(url);
        final JSONObject dataObject = (JSONObject) object.get("data");
        final JSONObject texturesObject = (JSONObject) dataObject.get("texture");
        return new Skin((String) texturesObject.get("value"), (String) texturesObject.get("signature"));
    });

    /**
     * Extracts a {@code Skin} object from a JSON representation.
     *
     * @param object the JSON object containing the skin data
     * @return the extracted {@code Skin}
     */
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

    /**
     * Represents a context used for providing a value to the {@code SkinAPI}.
     * This interface allows the {@code SkinAPI} to extract the necessary value for fetching skins.
     *
     * @param <V> the type of the context value
     */
    public interface Context<V> {

        /**
         * Returns the value associated with this context.
         *
         * @return the value of type {@code V}
         */
        V value();
    }
}
