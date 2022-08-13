package com.example.eqworkslocationlibrary

import com.example.eqworkslocationlibrary.service.LocationService
import com.example.eqworkslocationlibrary.model.LocationModel
import com.example.eqworkslocationlibrary.model.ResponseModel
import com.example.eqworkslocationlibrary.api.LocationClient
import kotlinx.coroutines.*
import okhttp3.MediaType
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.Call
import retrofit2.Callback

data class LocationEvent(val lat: Float,
                                val lon: Float,
                                val time: Long = System.currentTimeMillis(),
                                val ext: String = "")

class Library {
    fun setup(): Boolean {
        return true
    }

    /**
     * Entry point for library
     * This function validates the current location coordinates
     * POST these coordinates to the API server(httpbin) using Retrofit
     **/
    suspend fun log(event: LocationEvent): Response<ResponseModel> {
        // POST to API Server
        val latitude = if (event.lat >= -90 && event.lat <= 90) event.lat else 0.0f
        val longitude = if (event.lon >= -180 && event.lon <= 180) event.lon else 0.0f
        val time = event.time

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
                            mResponse.complete(Response.error(500, ResponseBody.create(MediaType.parse("application/json; charset\\u003dUTF-8"), "Something went Wrong")))
                        }
                    }
                    override fun onFailure(call: Call<ResponseModel>, t: Throwable) { // Handling failures
                        mResponse.complete(Response.error(500, ResponseBody.create(MediaType.parse("application/json; charset\\u003dUTF-8"), "Something went Wrong")))
                    }
                }
            )
        }

        return mResponse.await()
    }
}

