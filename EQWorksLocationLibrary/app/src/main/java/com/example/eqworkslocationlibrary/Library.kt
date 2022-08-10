package com.example.eqworkslocationlibrary

import com.example.eqworkslocationlibrary.service.LocationService
import com.example.eqworkslocationlibrary.model.LocationModel
import com.example.eqworkslocationlibrary.model.ResponseModel
import com.example.eqworkslocationlibrary.api.LocationClient
import kotlinx.coroutines.*
import retrofit2.Response
import retrofit2.Call
import retrofit2.Callback

public data class LocationEvent(val lat: Float,
                                val lon: Float,
                                val time: Long = System.currentTimeMillis(),
                                val ext: String = "")

public class Library {
    fun setup(): Boolean {
        return true
    }

    suspend fun log(event: LocationEvent): Response<ResponseModel> {
        // POST to API Server
        var latitude = 0.0f
        var longitude = 0.0f

        val time = event.time

        if (event.lat >= -90 && event.lat <= 90) latitude = event.lat

        if (event.lon >= -180 && event.lon <= 180) longitude = event.lon

        val locationModel = LocationModel(latitude, longitude, time)

        return requestLocation(locationModel)
    }

    private suspend fun requestLocation(locationModel: LocationModel) : Response<ResponseModel> {

        val mResponse = CompletableDeferred<Response<ResponseModel>>()
        CoroutineScope(Dispatchers.IO).launch {
            val service = LocationService.buildService(LocationClient::class.java)
            service.postLog(locationModel).enqueue(
                object : Callback<ResponseModel> {
                    override fun onResponse(
                        call: Call<ResponseModel>,
                        response: Response<ResponseModel>
                    ) {
                        if (response.isSuccessful) {
                            mResponse.complete(response)
                        } else {
                            mResponse.complete(response)
                        }
                    }

                    override fun onFailure(call: Call<ResponseModel>, t: Throwable) {
//                        print(t.toString())
                    }
                }
            )
        }

        return mResponse.await()
    }
}

