package com.github.tudeteam.telegram.thefreestuffbot.database;

import com.google.gson.annotations.SerializedName;

public enum GameApprovalStatus {
    @SerializedName("pending")
    PENDING,
    @SerializedName("declined")
    DECLINED,
    @SerializedName("accepted")
    ACCEPTED,
    @SerializedName("published")
    PUBLISHED,
    @SerializedName("scheduled")
    SCHEDULED
}
