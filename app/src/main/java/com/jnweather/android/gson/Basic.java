package com.jnweather.android.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Jason on 2017/6/22.
 */

public class Basic {

    @SerializedName("city")
    public String cityName;

    @SerializedName("id")
    public String weatherId;

    public Update update;

    public class Update{

        @SerializedName("loc")
        public String updateTime;
    }
}
