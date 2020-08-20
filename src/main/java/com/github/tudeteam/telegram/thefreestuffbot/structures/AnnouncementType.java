package com.github.tudeteam.telegram.thefreestuffbot.structures;

import com.google.gson.annotations.SerializedName;

public enum AnnouncementType {
    @SerializedName("free")
    FREE,
    @SerializedName("weekend")
    WEEKEND,
    @SerializedName("discount")
    DISCOUNT,
    @SerializedName("ad")
    AD,
    @SerializedName("unknown")
    UNKNOWN
}
