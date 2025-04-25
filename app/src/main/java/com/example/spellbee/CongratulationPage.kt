package com.example.spellbee

import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class CongratulationPage : AppCompatActivity() {

    private lateinit var totalWords:TextView
    private lateinit var correctWords:TextView
    private lateinit var wrongWords:TextView
    private lateinit var timer:TextView
    private lateinit var accuracyPercentage:TextView
    private lateinit var hintUsed:TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_congratulation_page)

        totalWords=findViewById(R.id.totalWords)
        correctWords=findViewById(R.id.correctWords)
        wrongWords=findViewById(R.id.wrongWords)
        timer=findViewById(R.id.timer)
        accuracyPercentage=findViewById(R.id.accuracyPercentage)
        hintUsed=findViewById(R.id.hintUsed)

        val total=intent.getIntExtra("totalWords",0)
        val correct=intent.getIntExtra("CorrectWords",0)
        val wrong=intent.getIntExtra("MistakeWords",0)
        val hint=intent.getIntExtra("HintUsed",0)

        totalWords.text="$total"
        correctWords.text="$correct"
        wrongWords.text="$wrong"
        hintUsed.text="$hint"

    }
}