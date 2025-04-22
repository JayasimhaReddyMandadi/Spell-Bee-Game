package com.example.spellbee

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeech.OnInitListener
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.spellbee.api.RetrofitClient
import kotlinx.coroutines.launch
import java.util.*

class GamePage : AppCompatActivity(), OnInitListener {

    private lateinit var speakerIcon: ImageView
    private lateinit var inputText:EditText
    private lateinit var submitButton:Button
    private lateinit var scoreButton:TextView
    private lateinit var hint_Button:LinearLayout
    private lateinit var textToSpeech: TextToSpeech
    private var wordList: List<String> = listOf()
    private var sentenceList: List<String> = listOf()
    private var currentIndex = 0
    private var score=0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_game_page)

        speakerIcon = findViewById(R.id.speakerIcon)
        inputText=findViewById(R.id.inputText)
        submitButton=findViewById(R.id.submitButton)
        scoreButton=findViewById(R.id.scoreButton)
        hint_Button=findViewById(R.id.hint_Button)

        textToSpeech = TextToSpeech(this, this)

        fetchSpellbeeData()

        speakerIcon.setOnClickListener {
            if (wordList.isNotEmpty()) {
                speakWord(wordList[currentIndex])

            }
        }
        hint_Button.setOnClickListener {
            if (sentenceList.isNotEmpty())
                speakSentence(sentenceList[currentIndex])
        }

        submitButton.setOnClickListener {
            val userInput=inputText.text.toString().trim()

            if(userInput.isNotEmpty()){
                val isCorrect=userInput.equals(wordList[currentIndex],ignoreCase = true)
                if (isCorrect){
                    score=score+10
                    scoreButton.text="$score"
                    currentIndex++
                    inputText.text.clear()

                    Toast.makeText(this,"Correct",Toast.LENGTH_SHORT).show()
                }else{
                    currentIndex++
                    inputText.text.clear()
                    Toast.makeText(this, "Incorrect, try again!", Toast.LENGTH_SHORT).show()
                }
            }
            else{
                Toast.makeText(this,"Please enter the word",Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun fetchSpellbeeData() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api.getSpellbeeData()
                wordList = response.words
                sentenceList=response.sentences
            } catch (e: Exception) {
                Log.e("GamePage", "Error: ${e.message}")
                Toast.makeText(this@GamePage, "Failed to load data", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun speakWord(word: String) {
        textToSpeech.speak(word, TextToSpeech.QUEUE_FLUSH, null, null)
    }
    private fun speakSentence(sentence: String) {
        textToSpeech.speak(sentence, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val langResult = textToSpeech.setLanguage(Locale.UK)
            if (langResult == TextToSpeech.LANG_MISSING_DATA || langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "Language not supported or missing data")
            } else {
                Log.d("TTS", "TextToSpeech initialized successfully")
            }
        } else {
            Log.e("TTS", "Initialization failed")
        }
    }

    override fun onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop()
            textToSpeech.shutdown()
        }
        super.onDestroy()
    }
}
