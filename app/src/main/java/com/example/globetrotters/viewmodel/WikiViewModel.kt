package com.example.globetrotters.viewmodel

import androidx.lifecycle.*
import com.example.globetrotters.api.Page
import com.example.globetrotters.api.WikipediaApi
import com.example.globetrotters.api.WikiResponse
import com.example.globetrotters.api.RetrofitInstance
import kotlinx.coroutines.launch
import retrofit2.Response

class WikiViewModel : ViewModel() {
    private val _extract = MutableLiveData<String?>()
    val extract: LiveData<String?> = _extract

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun fetchIntro(city: String) {
        viewModelScope.launch {
            try {
                val response: Response<WikiResponse> =
                    RetrofitInstance.wikipediaApi.getIntroExtract(titles = city)
                if (response.isSuccessful) {
                    val pages: Map<String, Page>? = response.body()?.query?.pages
                    val text = pages
                        ?.values
                        ?.firstOrNull()
                        ?.extract
                        ?: "Nessuna informazione trovata."
                    _extract.value = text
                } else {
                    _error.value = "Errore API Wikipedia: ${response.code()}"
                }
            } catch (t: Throwable) {
                _error.value = "Errore: ${t.localizedMessage}"
            }
        }
    }

    fun clear() {
        _extract.value = null
        _error.value = null
    }
}
