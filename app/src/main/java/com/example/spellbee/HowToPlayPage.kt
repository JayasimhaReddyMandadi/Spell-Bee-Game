package com.example.spellbee

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class HowToPlayPage : AppCompatActivity() {
    private lateinit var startGameButton:ImageButton
    private lateinit var backIcon:ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_how_to_play_page)

        startGameButton=findViewById(R.id.startGameButton)
        backIcon=findViewById(R.id.backIcon)

        startGameButton.setOnClickListener {
            val intent=Intent(this@HowToPlayPage,GamePage::class.java)
            startActivity(intent)
        }
        backIcon.setOnClickListener {
            val intent=Intent(this@HowToPlayPage,LandingPage::class.java)
            startActivity(intent)
        }
    }
}