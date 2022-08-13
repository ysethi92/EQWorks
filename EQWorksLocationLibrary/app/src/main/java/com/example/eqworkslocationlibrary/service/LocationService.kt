package com.example.eqworkslocationlibrary.service

import com.github.simonpercic.oklog3.OkLogInterceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object LocationService {

    /**
     * OkLog API provides link that contains a detailed response received from httpbin
     **/
    private val okLogInterceptor = OkLogInterceptor.builder().build()

    private val client = OkHttpClient.Builder().addInterceptor(okLogInterceptor).build()

    private val retrofit = Retrofit.Builder()
                            .baseUrl("https://httpbin.org/")
                            .addConverterFactory(GsonConverterFactory.create())
                            .client(client)
                            .build()

    fun<T> buildService(service: Class<T>): T{
        return retrofit.create(service)
    }
}