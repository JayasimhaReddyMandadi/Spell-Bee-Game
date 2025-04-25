package com.example.spellbee.api

import com.example.spellbee.data.SpellBeeResponce
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthService {
    @GET("api/spellbee/")
    suspend fun getSpellbeeData(): SpellBeeResponce

    @POST("submit/")
    suspend fun submitResults(@Body request: SubmitRequest): Response<Unit>
}
