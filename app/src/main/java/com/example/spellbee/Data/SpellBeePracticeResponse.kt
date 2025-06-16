package com.example.spellbee.data

data class SpellBeePracticeResponse(
    val words: List<PracticeWord>
)

data class PracticeWord(
    val word: String,
    val sentence: String
)