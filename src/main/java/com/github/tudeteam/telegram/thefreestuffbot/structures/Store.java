package com.github.tudeteam.telegram.thefreestuffbot.structures;

import com.google.gson.annotations.SerializedName;

public enum Store {
    @SerializedName("steam")
    STEAM("Steam"),
    @SerializedName("epic")
    EPIC("Epic Games Store"),
    @SerializedName("humble")
    HUMBLE("Humble Bundle"),
    @SerializedName("gog")
    GOG("GOG.com"),
    @SerializedName("origin")
    ORIGIN,
    @SerializedName("uplay")
    UPLAY("Uplay"),
    @SerializedName("twitch")
    TWITCH("Twitch"),
    @SerializedName("itch")
    ITCH("itch.io"),
    @SerializedName("discord")
    DISCORD("Discord"),
    @SerializedName("apple")
    APPLE("Apple App Store"),
    @SerializedName("google")
    GOOGLE("Google Play"),
    @SerializedName("switch")
    SWITCH("Nintendo Switch Store"),
    @SerializedName("ps")
    PS("Play Station"),
    @SerializedName("xbox")
    XBOX("Xbox"),
    @SerializedName("other")
    OTHER("Other");

    /**
     * The display name of the store.
     */
    private final String displayName;

    Store() {
        displayName = name();
    }

    Store(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Returns display name of the store.
     *
     * @return The display name of the store.
     */
    public String displayName() {
        return displayName;
    }

    /**
     * Formats the store name for usage in messages.
     *
     * @return The store name formatted for usage in messages.
     */
    @Override
    public String toString() {
        return displayName;
    }
}
