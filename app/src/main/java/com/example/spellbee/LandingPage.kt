package com.example.spellbee

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class LandingPage : AppCompatActivity() {

    private lateinit var startGameButton:Button
    private lateinit var practiceModeButton:Button
    private lateinit var howToPlayButton:Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_landing_page)

        startGameButton=findViewById(R.id.startGameButton)
        practiceModeButton=findViewById(R.id.practiceModeButton)
        howToPlayButton=findViewById(R.id.howToPlayButton)

        startGameButton.setOnClickListener {
            val intent=Intent(this@LandingPage,GamePage::class.java)
            startActivity(intent)
        }

        practiceModeButton.setOnClickListener {
            val intent = Intent(this@LandingPage, PracticePage::class.java)
            startActivity(intent)
        }

        howToPlayButton.setOnClickListener {
            val intent = Intent(this@LandingPage, HowToPlayPage::class.java)
            startActivity(intent)
        }
    }
}