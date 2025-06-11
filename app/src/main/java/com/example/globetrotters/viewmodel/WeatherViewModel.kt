package com.example.globetrotters.viewmodel

import androidx.lifecycle.*
import com.example.globetrotters.api.WeatherApi
import com.example.globetrotters.api.WeatherResponse
import com.example.globetrotters.api.RetrofitInstance
import kotlinx.coroutines.launch
import retrofit2.Response

data class WeatherResult(
    val temp: Double,
    val description: String,
    val humidity: Int,
    val pressure: Int,
    val windSpeed: Double
)

class WeatherViewModel : ViewModel() {

    private val _weather = MutableLiveData<WeatherResult?>()
    val weather: LiveData<WeatherResult?> = _weather

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun fetchCurrent(city: String, apiKey: String) {
        viewModelScope.launch {
            try {
                val response: Response<WeatherResponse> =
                    RetrofitInstance.weatherApi.getCurrentWeather(cityName = city, apiKey = apiKey)
                if (response.isSuccessful) {
                    response.body()?.let { body ->
                        val cond = body.weather.firstOrNull()?.description ?: "N/A"
                        val result = WeatherResult(
                            temp = body.main.temp,
                            description = cond,
                            humidity = body.main.humidity,
                            pressure = body.main.pressure,
                            windSpeed = body.wind.speed
                        )
                        _weather.value = result
                    } ?: run {
                        _error.value = "Risposta vuota"
                    }
                } else {
                    _error.value = "Errore API Meteo: ${response.code()}"
                }
            } catch (t: Throwable) {
                _error.value = "Errore: ${t.localizedMessage}"
            }
        }
    }

    fun clear() {
        _weather.value = null
        _error.value = null
    }
}
