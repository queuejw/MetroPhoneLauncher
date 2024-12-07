package com.kwabenaberko.openweathermaplib.model.threehourforecast

import com.google.gson.annotations.SerializedName

/**
 * Created by Kwabena Berko on 8/6/2017.
 */
class ThreeHourForecast {
    @SerializedName("cod")
    private val cod: String? = null

    @SerializedName("message")
    private val message = 0.0

    @SerializedName("cnt")
    private val cnt = 0

    @SerializedName("list")
    private val list: MutableList<ThreeHourForecastWeather?>? = null

    @SerializedName("city")
    private val city: City? = null


    fun getCod(): String? {
        return cod
    }

    fun getMessage(): Double {
        return message
    }

    fun getCnt(): Int {
        return cnt
    }

    fun getList(): MutableList<ThreeHourForecastWeather?>? {
        return list
    }

    fun getCity(): City? {
        return city
    }
}
