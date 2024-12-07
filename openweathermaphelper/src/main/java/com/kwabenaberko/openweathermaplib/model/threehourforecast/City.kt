package com.kwabenaberko.openweathermaplib.model.threehourforecast

import com.google.gson.annotations.SerializedName
import com.kwabenaberko.openweathermaplib.model.common.Coord

/**
 * Created by Kwabena Berko on 8/6/2017.
 */
class City {
    @SerializedName("id")
    private val id: Long = 0

    @SerializedName("name")
    private val name: String? = null

    @SerializedName("coord")
    private val coord: Coord? = null

    @SerializedName("country")
    private val country: String? = null

    @SerializedName("timezone")
    private val timezone: Long? = null

    @SerializedName("population")
    private val population: Long? = null

    @SerializedName("sunrise")
    private val sunrise: Long? = null

    @SerializedName("sunset")
    private val sunset: Long? = null

    fun getId(): Long {
        return id
    }

    fun getName(): String? {
        return name
    }

    fun getCoord(): Coord? {
        return coord
    }

    fun getCountry(): String? {
        return country
    }

    fun getTimezone(): Long? {
        return timezone
    }

    fun getPopulation(): Long? {
        return population
    }

    fun getSunrise(): Long? {
        return sunrise
    }

    fun getSunset(): Long? {
        return sunset
    }
}
