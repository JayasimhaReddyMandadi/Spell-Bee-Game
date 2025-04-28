package com.example.spellbee

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class CongratulationPage : AppCompatActivity() {

    private lateinit var totalWords: TextView
    private lateinit var correctWords: TextView
    private lateinit var wrongWords: TextView
    private lateinit var timer: TextView
    private lateinit var accuracyPercentage: TextView
    private lateinit var hintUsed: TextView
    private lateinit var playAgainButton:Button
    private lateinit var exitGameButton:Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_congratulation_page)

        totalWords = findViewById(R.id.totalWords)
        correctWords = findViewById(R.id.correctWords)
        wrongWords = findViewById(R.id.wrongWords)
        timer = findViewById(R.id.timer)
        accuracyPercentage = findViewById(R.id.accuracyPercentage)
        hintUsed = findViewById(R.id.hintUsed)
        playAgainButton=findViewById(R.id.playAgainButton)
        exitGameButton=findViewById(R.id.exitGameButton)

        val total = intent.getIntExtra("totalWords", 0)
        val correct = intent.getIntExtra("CorrectWords", 0)
        val wrong = intent.getIntExtra("MistakeWords", 0)
        val hint = intent.getIntExtra("HintUsed", 0)
        val time = intent.getIntExtra("TotalTime", 0)

        val percentage = if (total != 0) ((correct.toDouble() / total) * 100).toInt() else 0

        val minutes = time / 60
        val seconds = time % 60
        val formattedTime = String.format("%02dm:%02ds", minutes, seconds)

        totalWords.text = "$total"
        correctWords.text = "$correct"
        wrongWords.text = "$wrong"
        hintUsed.text = "$hint"
        accuracyPercentage.text = "$percentage%"
        timer.text = formattedTime

        playAgainButton.setOnClickListener {
            val intent=Intent(this@CongratulationPage,GamePage::class.java)
            startActivity(intent)
            finish()
        }

        exitGameButton.setOnClickListener {
            val intent=Intent(this@CongratulationPage,LandingPage::class.java)
            startActivity(intent)
            finish()
        }
    }
}
