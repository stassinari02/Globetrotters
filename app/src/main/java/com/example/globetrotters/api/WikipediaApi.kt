package com.example.globetrotters.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface WikipediaApi {
    @GET("w/api.php")
    suspend fun getIntroExtract(
        @Query("action") action: String = "query",
        @Query("format") format: String = "json",
        @Query("prop") prop: String = "extracts",
        @Query("exintro") exintro: Boolean = true,
        @Query("explaintext") explaintext: Boolean = true,
        @Query("titles") titles: String
    ): Response<WikiResponse>
}
