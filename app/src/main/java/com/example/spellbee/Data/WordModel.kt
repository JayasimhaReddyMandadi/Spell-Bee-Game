package com.example.spellbee.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class WordModel(
    val word: String,
    val sentence: String,
    var isIncorrect: Boolean = false,
    var isSkipped: Boolean = false
) : Parcelable 