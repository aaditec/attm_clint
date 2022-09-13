package com.technosale.atm.Model;

import com.google.gson.annotations.SerializedName;

public class AudioResponseModel {
    public String audio_base64_text;
    @SerializedName("Acknowledge")
    public String acknowledge;
    public String time;
}
