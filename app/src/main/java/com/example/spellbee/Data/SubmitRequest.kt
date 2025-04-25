package com.example.spellbee.api

data class SubmitRequest(
    val rider_id: Int,
    val event_id: String,
    val mistake_words: Int,
    val correct_words: Int,
    val total_points: Int,
    val submit_date: String
)
