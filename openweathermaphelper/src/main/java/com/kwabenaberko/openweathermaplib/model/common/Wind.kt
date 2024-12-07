package com.kwabenaberko.openweathermaplib.model.common

import com.google.gson.annotations.SerializedName

/**
 * Created by Kwabena Berko on 7/25/2017.
 */
class Wind {
    @SerializedName("speed")
    private val speed = 0.0

    @SerializedName("deg")
    private val deg = 0.0

    @SerializedName("gust")
    private val gust: Double? = null

    fun getSpeed(): Double {
        return speed
    }

    fun getDeg(): Double {
        return deg
    }

    fun getGust(): Double? {
        return gust
    }
}
