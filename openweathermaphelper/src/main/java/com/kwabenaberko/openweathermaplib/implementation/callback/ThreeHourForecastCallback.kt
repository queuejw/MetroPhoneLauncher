package com.kwabenaberko.openweathermaplib.implementation.callback

import com.kwabenaberko.openweathermaplib.model.threehourforecast.ThreeHourForecast

interface ThreeHourForecastCallback {
    fun onSuccess(threeHourForecast: ThreeHourForecast?)
    fun onFailure(throwable: Throwable?)
}