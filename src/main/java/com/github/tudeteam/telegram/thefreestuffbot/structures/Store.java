package com.github.tudeteam.telegram.thefreestuffbot.structures;

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
    OTHER;

    @Override
    public String toString() {
        switch (this) {
            case STEAM:
                return "Steam";
            case EPIC:
                return "Epic Games Store";
            case HUMBLE:
                return "Humble Bundle";
            case GOG:
                return "GOG.com";
            case UPLAY:
                return "Uplay";
            case TWITCH:
                return "Twitch";
            case ITCH:
                return "itch.io";
            case DISCORD:
                return "Discord";
            case APPLE:
                return "Apple App Store";
            case GOOGLE:
                return "Google Play";
            case SWITCH:
                return "Nintendo Switch Store";
            case PS:
                return "Play Station";
            case XBOX:
                return "Xbox";
            case OTHER:
                return "Other";
            default:
                return name();
        }
    }
}
