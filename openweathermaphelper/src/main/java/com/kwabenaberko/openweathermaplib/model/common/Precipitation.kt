package com.kwabenaberko.openweathermaplib.model.common

import com.google.gson.annotations.SerializedName

open class Precipitation {
    @SerializedName("1h")
    private val oneHour: Double? = null

    @SerializedName("3h")
    private val threeHour: Double? = null

    fun getOneHour(): Double? {
        return oneHour
    }

    fun getThreeHour(): Double? {
        return threeHour
    }
}
