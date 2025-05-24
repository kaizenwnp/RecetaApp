package com.example.recetaapp.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "https://www.themealdb.com/api/json/v1/1/"

    // Lazy initialization of Retrofit API
    val api: TheMealDbApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)  // Base URL for TheMealDB API
            .addConverterFactory(GsonConverterFactory.create())  // Use Gson for JSON conversion
            .build()
            .create(TheMealDbApi::class.java)  // Create API instance
    }
}
