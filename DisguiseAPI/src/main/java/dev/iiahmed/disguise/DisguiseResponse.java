package dev.iiahmed.disguise;

/**
 * The response returned when disgusing
 */
public enum DisguiseResponse {

    /**
     * Returns when the MC version of the server is NOT supported
     * which most likely is NOT going to happen
     */
    FAIL_VERSION_NOT_SUPPORTED,
    /**
     * Returns when the Plugin is either not initialized or disabled
     */
    FAIL_PLUGIN_NOT_INITIALIZED,
    /**
     * Returns when the given disguise has no new Name, Skin or EntityType
     */
    FAIL_EMPTY_DISGUISE,
    /**
     * Returns when the given EntityType is NOT supported
     */
    FAIL_ENTITY_NOT_SUPPORTED,
    /**
     * Returns when there's an online player with the picked name
     */
    FAIL_NAME_ALREADY_ONLINE,
    /**
     * Returns when reflections fail to get the player's GameProfile
     */
    FAIL_PROFILE_NOT_FOUND,
    /**
     * Returns when reflections fail to change the player's GameProfile's name
     * which most likely is NOT going to happen
     */
    FAIL_NAME_CHANGE_EXCEPTION,
    /**
     * Returns when the disguise request succeeds
     */
    SUCCESS

}
