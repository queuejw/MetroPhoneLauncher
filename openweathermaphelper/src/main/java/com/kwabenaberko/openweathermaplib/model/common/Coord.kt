package com.kwabenaberko.openweathermaplib.model.common

import com.google.gson.annotations.SerializedName

/**
 * Created by Kwabena Berko on 7/25/2017.
 */
class Coord {
    @SerializedName("lon")
    private val lon = 0.0

    @SerializedName("lat")
    private val lat = 0.0

    fun getLon(): Double {
        return lon
    }

    fun getLat(): Double {
        return lat
    }
}
