package com.kwabenaberko.openweathermaplib.model.threehourforecast

import com.google.gson.annotations.SerializedName
import com.kwabenaberko.openweathermaplib.model.common.Clouds
import com.kwabenaberko.openweathermaplib.model.common.Main
import com.kwabenaberko.openweathermaplib.model.common.Rain
import com.kwabenaberko.openweathermaplib.model.common.Snow
import com.kwabenaberko.openweathermaplib.model.common.Sys
import com.kwabenaberko.openweathermaplib.model.common.Weather
import com.kwabenaberko.openweathermaplib.model.common.Wind

/**
 * Created by Kwabena Berko on 8/6/2017.
 */
class ThreeHourForecastWeather {
    @SerializedName("dt")
    private var dt: Long? = null

    @SerializedName("main")
    private var main: Main? = null

    @SerializedName("weather")
    private var weather: MutableList<Weather?>? = null

    @SerializedName("clouds")
    private var clouds: Clouds? = null

    @SerializedName("wind")
    private var wind: Wind? = null

    @SerializedName("visibility")
    private val visibility: Long? = null

    @SerializedName("pop")
    private val pop: Double? = null

    @SerializedName("rain")
    private var rain: Rain? = null

    @SerializedName("snow")
    private var snow: Snow? = null

    @SerializedName("sys")
    private var mSys: Sys? = null

    @SerializedName("dt_txt")
    private var dtTxt: String? = null

    fun getDt(): Long? {
        return dt
    }

    fun setDt(dt: Long?) {
        this.dt = dt
    }

    fun getMain(): Main? {
        return main
    }

    fun setMain(main: Main?) {
        this.main = main
    }

    fun getWeather(): MutableList<Weather?>? {
        return weather
    }

    fun setWeather(weather: MutableList<Weather?>?) {
        this.weather = weather
    }

    fun getClouds(): Clouds? {
        return clouds
    }

    fun setClouds(clouds: Clouds?) {
        this.clouds = clouds
    }

    fun getWind(): Wind? {
        return wind
    }

    fun getVisibility(): Long? {
        return visibility
    }

    fun getPop(): Double? {
        return pop
    }

    fun setWind(wind: Wind?) {
        this.wind = wind
    }

    fun getRain(): Rain? {
        return rain
    }

    fun setRain(rain: Rain?) {
        this.rain = rain
    }

    fun getSnow(): Snow? {
        return snow
    }

    fun setSnow(snow: Snow?) {
        this.snow = snow
    }

    fun getmSys(): Sys? {
        return mSys
    }

    fun setmSys(mSys: Sys?) {
        this.mSys = mSys
    }

    fun getDtTxt(): String? {
        return dtTxt
    }

    fun setDtTxt(dtTxt: String?) {
        this.dtTxt = dtTxt
    }
}
