package com.kwabenaberko.openweathermaplib.model.currentweather

import com.google.gson.annotations.SerializedName
import com.kwabenaberko.openweathermaplib.model.common.Clouds
import com.kwabenaberko.openweathermaplib.model.common.Coord
import com.kwabenaberko.openweathermaplib.model.common.Main
import com.kwabenaberko.openweathermaplib.model.common.Rain
import com.kwabenaberko.openweathermaplib.model.common.Snow
import com.kwabenaberko.openweathermaplib.model.common.Sys
import com.kwabenaberko.openweathermaplib.model.common.Weather
import com.kwabenaberko.openweathermaplib.model.common.Wind

/**
 * Created by Kwabena Berko on 7/25/2017.
 */
class CurrentWeather {
    @SerializedName("coord")
    private val coord: Coord? = null

    @SerializedName("weather")
    private val weather: MutableList<Weather?>? = null

    @SerializedName("base")
    private val base: String? = null

    @SerializedName("main")
    private val main: Main? = null

    @SerializedName("visibility")
    private val visibility: Long? = null

    @SerializedName("wind")
    private val wind: Wind? = null

    @SerializedName("clouds")
    private val clouds: Clouds? = null

    @SerializedName("rain")
    private val rain: Rain? = null

    @SerializedName("snow")
    private val snow: Snow? = null

    @SerializedName("dt")
    private val dt: Long? = null

    @SerializedName("sys")
    private val sys: Sys? = null

    @SerializedName("timezone")
    private val timezone: Long? = null

    @SerializedName("id")
    private val id: Long? = null

    @SerializedName("name")
    private val name: String? = null

    @SerializedName("cod")
    private val cod: Int? = null

    fun getCoord(): Coord? {
        return coord
    }

    fun getWeather(): MutableList<Weather?>? {
        return weather
    }

    fun getBase(): String? {
        return base
    }

    fun getMain(): Main? {
        return main
    }

    fun getVisibility(): Long? {
        return visibility
    }

    fun getWind(): Wind? {
        return wind
    }

    fun getClouds(): Clouds? {
        return clouds
    }

    fun getRain(): Rain? {
        return rain
    }

    fun getSnow(): Snow? {
        return snow
    }

    fun getDt(): Long? {
        return dt
    }

    fun getSys(): Sys? {
        return sys
    }

    fun getTimezone(): Long? {
        return timezone
    }

    fun getId(): Long? {
        return id
    }

    fun getName(): String? {
        return name
    }

    fun getCod(): Int? {
        return cod
    }
}
