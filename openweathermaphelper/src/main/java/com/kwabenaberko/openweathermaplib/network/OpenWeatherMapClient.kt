package com.kwabenaberko.openweathermaplib.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Created by Kwabena Berko on 7/25/2017.
 */
object OpenWeatherMapClient {
    private const val BASE_URL = "https://api.openweathermap.org"
    private var retrofit: Retrofit? = null
    fun getClient(): Retrofit {
        if (retrofit == null) {
            retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
        return retrofit!!
    }
}
