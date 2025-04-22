package com.example.spellbee.data

data class SpellBeeResponce(
    val spellbee_id: Int,
    val date: String,
    val words: List<String>,
    val sentences: List<String>,
    val status: Boolean
)