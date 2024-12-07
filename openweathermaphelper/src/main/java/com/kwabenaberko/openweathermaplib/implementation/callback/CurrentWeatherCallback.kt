package com.kwabenaberko.openweathermaplib.implementation.callback

import com.kwabenaberko.openweathermaplib.model.currentweather.CurrentWeather

interface CurrentWeatherCallback {
    fun onSuccess(currentWeather: CurrentWeather?)
    fun onFailure(throwable: Throwable?)
}
