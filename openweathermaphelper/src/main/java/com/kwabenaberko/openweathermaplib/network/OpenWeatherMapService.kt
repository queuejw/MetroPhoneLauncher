package com.kwabenaberko.openweathermaplib.network

import com.kwabenaberko.openweathermaplib.model.currentweather.CurrentWeather
import com.kwabenaberko.openweathermaplib.model.threehourforecast.ThreeHourForecast
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.QueryMap

/**
 * Created by Kwabena Berko on 7/25/2017.
 */
interface OpenWeatherMapService {
    //Current Weather Endpoints start
    @GET(CURRENT)
    fun getCurrentWeatherByCityName(@QueryMap options: MutableMap<String?, String?>?): Call<CurrentWeather?>?

    @GET(CURRENT)
    fun getCurrentWeatherByCityID(@QueryMap options: MutableMap<String?, String?>?): Call<CurrentWeather?>?

    @GET(CURRENT)
    fun getCurrentWeatherByGeoCoordinates(@QueryMap options: MutableMap<String?, String?>?): Call<CurrentWeather?>?

    @GET(CURRENT)
    fun getCurrentWeatherByZipCode(@QueryMap options: MutableMap<String?, String?>?): Call<CurrentWeather?>?

    //Current Weather Endpoints end
    //Three hour forecast endpoints start
    @GET(FORECAST)
    fun getThreeHourForecastByCityName(@QueryMap options: MutableMap<String?, String?>?): Call<ThreeHourForecast?>?

    @GET(FORECAST)
    fun getThreeHourForecastByCityID(@QueryMap options: MutableMap<String?, String?>?): Call<ThreeHourForecast?>?

    @GET(FORECAST)
    fun getThreeHourForecastByGeoCoordinates(@QueryMap options: MutableMap<String?, String?>?): Call<ThreeHourForecast?>?

    @GET(FORECAST)
    fun getThreeHourForecastByZipCode(@QueryMap options: MutableMap<String?, String?>?): Call<ThreeHourForecast?>?

    companion object {
        const val CURRENT: String = "/data/2.5/weather"
        const val FORECAST: String = "/data/2.5/forecast"
    }
}
