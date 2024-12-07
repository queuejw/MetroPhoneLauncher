package com.kwabenaberko.openweathermaplib.model.common

import com.google.gson.annotations.SerializedName

/**
 * Created by Kwabena Berko on 7/25/2017.
 */
class Weather {
    @SerializedName("id")
    private val id: Long? = null

    @SerializedName("main")
    private val main: String? = null

    @SerializedName("description")
    private val description: String? = null

    @SerializedName("icon")
    private val icon: String? = null

    fun getId(): Long? {
        return id
    }

    fun getMain(): String? {
        return main
    }

    fun getDescription(): String? {
        return description
    }

    fun getIcon(): String? {
        return icon
    }
}
