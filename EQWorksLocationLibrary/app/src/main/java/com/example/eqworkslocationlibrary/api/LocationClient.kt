package com.example.eqworkslocationlibrary.api

import com.example.eqworkslocationlibrary.model.LocationModel
import com.example.eqworkslocationlibrary.model.ResponseModel
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface LocationClient {

    @POST("/post")
    fun postLog(@Body locationModel: LocationModel): Call<ResponseModel>
}