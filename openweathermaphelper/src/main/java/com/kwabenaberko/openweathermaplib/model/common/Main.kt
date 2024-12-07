package com.kwabenaberko.openweathermaplib.model.common

import com.google.gson.annotations.SerializedName

/**
 * Created by Kwabena Berko on 7/25/2017.
 */
class Main {
    @SerializedName("temp")
    private val temp = 0.0

    @SerializedName("feels_like")
    private val feelsLike = 0.0

    @SerializedName("temp_min")
    private val tempMin = 0.0

    @SerializedName("temp_max")
    private val tempMax = 0.0

    @SerializedName("pressure")
    private val pressure = 0.0

    @SerializedName("humidity")
    private val humidity = 0.0

    @SerializedName("sea_level")
    private val seaLevel: Double? = null

    @SerializedName("grnd_level")
    private val grndLevel: Double? = null

    @SerializedName("temp_kf")
    private val tempKf: Double? = null


    fun getTemp(): Double {
        return temp
    }

    fun getFeelsLike(): Double {
        return feelsLike
    }

    fun getTempMin(): Double {
        return tempMin
    }

    fun getTempMax(): Double {
        return tempMax
    }

    fun getPressure(): Double {
        return pressure
    }

    fun getHumidity(): Double {
        return humidity
    }

    fun getSeaLevel(): Double? {
        return seaLevel
    }

    fun getGrndLevel(): Double? {
        return grndLevel
    }

    fun getTempKf(): Double? {
        return tempKf
    }
}
