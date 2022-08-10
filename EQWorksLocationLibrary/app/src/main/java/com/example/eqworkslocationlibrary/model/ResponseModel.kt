package com.example.eqworkslocationlibrary.model

import com.google.gson.annotations.SerializedName

data class ResponseModel (
    @SerializedName("data") val data: String,
    @SerializedName("origin") val origin: String,
    @SerializedName("url") val url: String
)