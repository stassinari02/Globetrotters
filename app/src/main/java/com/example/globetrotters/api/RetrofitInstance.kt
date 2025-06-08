package com.example.globetrotters.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {

    // Retrofit per Wikipedia
    private val wikipediaRetrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://it.wikipedia.org/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val wikipediaApi: WikipediaApi by lazy {
        wikipediaRetrofit.create(WikipediaApi::class.java)
    }

    // Retrofit per OpenWeatherMap (meteo)
    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val weatherApi: WeatherApi by lazy {
        retrofit.create(WeatherApi::class.java)
    }
}