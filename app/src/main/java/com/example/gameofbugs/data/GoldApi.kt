package com.example.gameofbugs.data

import retrofit2.http.GET
import retrofit2.http.Query

interface GoldApi {
    @GET("scripts/xml_metall.asp")
    suspend fun getMetall(
        @Query("date_req1") from: String,
        @Query("date_req2") to: String
    ): String
}
