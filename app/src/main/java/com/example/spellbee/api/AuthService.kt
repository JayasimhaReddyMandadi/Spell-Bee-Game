package com.example.spellbee.api

import com.example.spellbee.data.SpellBeeResponce
import retrofit2.http.GET

interface AuthService {
    @GET("api/spellbee/")
    suspend fun getSpellbeeData(): SpellBeeResponce
}
