package com.github.tudeteam.telegram.thefreestuffbot.database;

import com.google.gson.annotations.SerializedName;

public enum Store {
    @SerializedName("steam")
    STEAM,
    @SerializedName("epic")
    EPIC,
    @SerializedName("humble")
    HUMBLE,
    @SerializedName("gog")
    GOG,
    @SerializedName("origin")
    ORIGIN,
    @SerializedName("uplay")
    UPLAY,
    @SerializedName("twitch")
    TWITCH,
    @SerializedName("itch")
    ITCH,
    @SerializedName("discord")
    DISCORD,
    @SerializedName("apple")
    APPLE,
    @SerializedName("google")
    GOOGLE,
    @SerializedName("switch")
    SWITCH,
    @SerializedName("ps")
    PS,
    @SerializedName("xbox")
    XBOX,
    @SerializedName("other")
    OTHER
}
