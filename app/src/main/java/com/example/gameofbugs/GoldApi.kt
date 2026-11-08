package com.example.gameofbugs

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface GoldApi {
    @GET("scripts/xml_metall.asp")
    fun getMetall(
        @Query("date_req1") from: String,
        @Query("date_req2") to: String
    ): Call<String>
}
