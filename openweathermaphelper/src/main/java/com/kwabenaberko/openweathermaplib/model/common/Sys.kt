package com.kwabenaberko.openweathermaplib.model.common

import com.google.gson.annotations.SerializedName

/**
 * Created by Kwabena Berko on 7/25/2017.
 */
class Sys {
    @SerializedName("type")
    private val type = 0.0

    @SerializedName("id")
    private val id: Long? = null

    @SerializedName("message")
    private val message: Double? = null

    @SerializedName("country")
    private val country: String? = null

    @SerializedName("sunrise")
    private val sunrise: Long? = null

    @SerializedName("sunset")
    private val sunset: Long? = null

    @SerializedName("pod")
    private val pod: Char? = null

    fun getType(): Double {
        return type
    }

    fun getId(): Long? {
        return id
    }

    fun getMessage(): Double? {
        return message
    }

    fun getCountry(): String? {
        return country
    }

    fun getSunrise(): Long? {
        return sunrise
    }

    fun getSunset(): Long? {
        return sunset
    }

    fun getPod(): Char? {
        return pod
    }
}
