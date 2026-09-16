package com.example.plomaap.data.api

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit

object ApiClient {
    private const val PRIMARY_HOST = "127.0.0.1"       // USB ADB reverse
    private const val WIFI_HOST = "10.1.193.174"      // Wi-Fi LAN IP
    private const val EMULATOR_HOST = "10.0.2.2"       // Android Emulator
    private const val PORT = 5000

    const val BASE_URL = "http://$PRIMARY_HOST:$PORT/"

    private val fallbackInterceptor = Interceptor { chain ->
        val originalRequest = chain.request()
        try {
            chain.proceed(originalRequest)
        } catch (e: IOException) {
            // Fallback 1: Wi-Fi LAN
            val url = originalRequest.url
            val newUrlWifi = url.newBuilder().host(WIFI_HOST).build()
            val newRequestWifi = originalRequest.newBuilder().url(newUrlWifi).build()
            try {
                chain.proceed(newRequestWifi)
            } catch (e2: IOException) {
                // Fallback 2: Android Emulator
                val newUrlEmu = url.newBuilder().host(EMULATOR_HOST).build()
                val newRequestEmu = originalRequest.newBuilder().url(newUrlEmu).build()
                chain.proceed(newRequestEmu)
            }
        }
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(fallbackInterceptor)
        .addInterceptor(loggingInterceptor)
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val apiService: ApiService = retrofit.create(ApiService::class.java)
}
