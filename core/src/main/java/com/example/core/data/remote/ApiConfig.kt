package com.example.core.data.remote

import com.example.core.BuildConfig
import com.example.core.data.remote.ApiService.Companion.TOKEN
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ApiConfig {
    companion object {
        fun getApiService(): ApiService {
            val header = Interceptor {
                val req =
                    it.request().newBuilder().addHeader("Authorization", "Bearer $TOKEN").build()
                it.proceed(req)
            }
            val client =
                OkHttpClient.Builder().addInterceptor(header)
            if (BuildConfig.DEBUG) {
                val logging = HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
                client.addInterceptor(logging)
            }
            val retrofit = Retrofit.Builder().baseUrl("https://api.themoviedb.org/3/")
                .addConverterFactory(GsonConverterFactory.create()).client(client.build()).build()
            return retrofit.create(ApiService::class.java)
        }
    }
}