package com.kwabenaberko.openweathermaplib.implementation

import com.kwabenaberko.openweathermaplib.implementation.callback.CurrentWeatherCallback
import com.kwabenaberko.openweathermaplib.implementation.callback.ThreeHourForecastCallback
import com.kwabenaberko.openweathermaplib.model.currentweather.CurrentWeather
import com.kwabenaberko.openweathermaplib.model.threehourforecast.ThreeHourForecast
import com.kwabenaberko.openweathermaplib.network.OpenWeatherMapClient
import com.kwabenaberko.openweathermaplib.network.OpenWeatherMapService
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.net.HttpURLConnection
import java.util.HashMap

/**
 * Created by Kwabena Berko on 7/25/2017.
 */
class OpenWeatherMapHelper(apiKey: String?) {
    private val openWeatherMapService: OpenWeatherMapService = OpenWeatherMapClient.getClient()
        .create<OpenWeatherMapService>(OpenWeatherMapService::class.java)
    private val options = HashMap<String?, String?>()


    init {
        options.put(APPID, if (apiKey == null) "" else apiKey)
    }


    //SETUP METHODS
    fun setUnits(units: String?) {
        options.put(UNITS, units)
    }

    fun setLanguage(lang: String?) {
        options.put(LANGUAGE, lang)
    }


    private fun noAppIdErrMessage(): Throwable {
        return Throwable("UnAuthorized. Please set a valid OpenWeatherMap API KEY.")
    }


    private fun notFoundErrMsg(str: String): Throwable {
        var throwable: Throwable? = null
        try {
            val obj = JSONObject(str)
            throwable = Throwable(obj.getString("message"))
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        if (throwable == null) {
            throwable = Throwable("An unexpected error occurred.")
        }


        return throwable
    }

    //    CURRENT WEATHER METHODS
    //    START
    //GET CURRENT WEATHER BY CITY NAME
    fun getCurrentWeatherByCityName(city: String?, callback: CurrentWeatherCallback) {
        options.put(QUERY, city)

        openWeatherMapService.getCurrentWeatherByCityName(options)?.enqueue(object : Callback<CurrentWeather?> {
                override fun onResponse(
                    call: Call<CurrentWeather?>,
                    response: Response<CurrentWeather?>
                ) {
                    handleCurrentWeatherResponse(response, callback)
                }

                override fun onFailure(call: Call<CurrentWeather?>, throwable: Throwable) {
                    callback.onFailure(throwable)
                }
            })
    }

    //GET CURRENT WEATHER BY CITY ID
    fun getCurrentWeatherByCityID(id: String?, callback: CurrentWeatherCallback) {
        options.put(CITY_ID, id)
        openWeatherMapService.getCurrentWeatherByCityID(options)?.enqueue(object : Callback<CurrentWeather?> {
                override fun onResponse(
                    call: Call<CurrentWeather?>?,
                    response: Response<CurrentWeather?>?
                ) {
                    handleCurrentWeatherResponse(response!!, callback)
                }

                override fun onFailure(call: Call<CurrentWeather?>, throwable: Throwable) {
                    callback.onFailure(throwable)
                }
            })
    }

    //GET CURRENT WEATHER BY GEOGRAPHIC COORDINATES
    fun getCurrentWeatherByGeoCoordinates(
        latitude: Double,
        longitude: Double,
        callback: CurrentWeatherCallback
    ) {
        options.put(LATITUDE, latitude.toString())
        options.put(LONGITUDE, longitude.toString())
        openWeatherMapService.getCurrentWeatherByGeoCoordinates(options)?.enqueue(object : Callback<CurrentWeather?> {
                override fun onResponse(
                    call: Call<CurrentWeather?>?,
                    response: Response<CurrentWeather?>?
                ) {
                    handleCurrentWeatherResponse(response!!, callback)
                }

                override fun onFailure(call: Call<CurrentWeather?>, throwable: Throwable) {
                    callback.onFailure(throwable)
                }
            })
    }

    //GET CURRENT WEATHER BY ZIP CODE
    fun getCurrentWeatherByZipCode(zipCode: String?, callback: CurrentWeatherCallback) {
        options.put(ZIP_CODE, zipCode)
        openWeatherMapService.getCurrentWeatherByZipCode(options)?.enqueue(object : Callback<CurrentWeather?> {
                override fun onResponse(
                    call: Call<CurrentWeather?>?,
                    response: Response<CurrentWeather?>?
                ) {
                    handleCurrentWeatherResponse(response!!, callback)
                }

                override fun onFailure(call: Call<CurrentWeather?>, throwable: Throwable) {
                    callback.onFailure(throwable)
                }
            })
    }

    private fun handleCurrentWeatherResponse(
        response: Response<CurrentWeather?>,
        callback: CurrentWeatherCallback
    ) {
        if (response.code() == HttpURLConnection.HTTP_OK) {
            callback.onSuccess(response.body())
        } else if (response.code() == HttpURLConnection.HTTP_FORBIDDEN || response.code() == HttpURLConnection.HTTP_UNAUTHORIZED) {
            callback.onFailure(noAppIdErrMessage())
        } else {
            try {
                callback.onFailure(notFoundErrMsg(response.errorBody()!!.string()))
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }


    //    CURRENT WEATHER METHODS
    //    END
    //    THREE HOUR FORECAST METHODS
    //    START
    //GET THREE HOUR FORECAST BY CITY NAME
    fun getThreeHourForecastByCityName(city: String?, callback: ThreeHourForecastCallback) {
        options.put(QUERY, city)
        openWeatherMapService.getThreeHourForecastByCityName(options)?.enqueue(object : Callback<ThreeHourForecast?> {
                override fun onResponse(
                    call: Call<ThreeHourForecast?>,
                    response: Response<ThreeHourForecast?>
                ) {
                    handleThreeHourForecastResponse(response, callback)
                }

                override fun onFailure(call: Call<ThreeHourForecast?>, throwable: Throwable) {
                    callback.onFailure(throwable)
                }
            })
    }

    //GET THREE HOUR FORECAST BY CITY ID
    fun getThreeHourForecastByCityID(id: String?, callback: ThreeHourForecastCallback) {
        options.put(CITY_ID, id)
        openWeatherMapService.getThreeHourForecastByCityID(options)?.enqueue(object : Callback<ThreeHourForecast?> {
                override fun onResponse(
                    call: Call<ThreeHourForecast?>,
                    response: Response<ThreeHourForecast?>
                ) {
                    handleThreeHourForecastResponse(response, callback)
                }

                override fun onFailure(call: Call<ThreeHourForecast?>, throwable: Throwable) {
                    callback.onFailure(throwable)
                }
            })
    }

    //GET THREE HOUR FORECAST BY GEO C0ORDINATES
    fun getThreeHourForecastByGeoCoordinates(
        latitude: Double,
        longitude: Double,
        callback: ThreeHourForecastCallback
    ) {
        options.put(LATITUDE, latitude.toString())
        options.put(LONGITUDE, longitude.toString())
        openWeatherMapService.getThreeHourForecastByGeoCoordinates(options)?.enqueue(object : Callback<ThreeHourForecast?> {
                override fun onResponse(
                    call: Call<ThreeHourForecast?>,
                    response: Response<ThreeHourForecast?>
                ) {
                    handleThreeHourForecastResponse(response, callback)
                }

                override fun onFailure(call: Call<ThreeHourForecast?>, throwable: Throwable) {
                    callback.onFailure(throwable)
                }
            })
    }

    //GET THREE HOUR FORECAST BY ZIP CODE
    fun getThreeHourForecastByZipCode(zipCode: String?, callback: ThreeHourForecastCallback) {
        options.put(ZIP_CODE, zipCode)
        openWeatherMapService.getThreeHourForecastByZipCode(options)?.enqueue(object : Callback<ThreeHourForecast?> {
                override fun onResponse(
                    call: Call<ThreeHourForecast?>,
                    response: Response<ThreeHourForecast?>
                ) {
                    handleThreeHourForecastResponse(response, callback)
                }

                override fun onFailure(call: Call<ThreeHourForecast?>, throwable: Throwable) {
                    callback.onFailure(throwable)
                }
            })
    }

    private fun handleThreeHourForecastResponse(
        response: Response<ThreeHourForecast?>,
        callback: ThreeHourForecastCallback
    ) {
        if (response.code() == HttpURLConnection.HTTP_OK) {
            callback.onSuccess(response.body())
        } else if (response.code() == HttpURLConnection.HTTP_FORBIDDEN || response.code() == HttpURLConnection.HTTP_UNAUTHORIZED) {
            callback.onFailure(noAppIdErrMessage())
        } else {
            try {
                callback.onFailure(notFoundErrMsg(response.errorBody()!!.string()))
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }


    companion object {
        private const val APPID = "appId"
        private const val UNITS = "units"
        private const val LANGUAGE = "lang"
        private const val QUERY = "q"
        private const val CITY_ID = "id"
        private const val LATITUDE = "lat"
        private const val LONGITUDE = "lon"
        private const val ZIP_CODE = "zip"
    }
}
