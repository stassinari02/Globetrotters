package com.example.globetrotters.api

data class WeatherResponse(
    val weather: List<WeatherCondition>,
    val main: MainWeather,
    val wind: Wind
)

data class WeatherCondition(
    val main: String,
    val description: String
)

data class MainWeather(
    val temp: Double,
    val humidity: Int,
    val pressure: Int
)

data class Wind(
    val speed: Double
)