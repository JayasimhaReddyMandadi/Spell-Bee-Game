package com.example.spellbee

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeech.OnInitListener
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.spellbee.api.SubmitRequest
import com.example.spellbee.api.RetrofitClient
import kotlinx.coroutines.launch
import java.util.*

class GamePage : AppCompatActivity(), OnInitListener {

    private lateinit var speakerIcon: ImageView
    private lateinit var inputText:EditText
    private lateinit var submitButton:Button
    private lateinit var scoreButton:TextView
    private lateinit var hint_Button:LinearLayout
    private lateinit var timerButton:TextView
    private lateinit var wordCountText:TextView
    private lateinit var updateProgressbar:ProgressBar
    private lateinit var textToSpeech: TextToSpeech
    private var wordList: List<String> = listOf()
    private var sentenceList: List<String> = listOf()
    private var currentIndex = 0
    private var score=0
    private var countDownTimer: CountDownTimer? = null
    private val TIME_PER_WORD = 20000L
    private var hintUsed=0
    private var hintUsedForCurrentWord = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_game_page)

        speakerIcon = findViewById(R.id.speakerIcon)
        inputText=findViewById(R.id.inputText)
        submitButton=findViewById(R.id.submitButton)
        scoreButton=findViewById(R.id.scoreButton)
        hint_Button=findViewById(R.id.hint_Button)
        timerButton=findViewById(R.id.timerButton)
        wordCountText=findViewById(R.id.wordCountText)
        updateProgressbar=findViewById(R.id.updateProgressbar)

        textToSpeech = TextToSpeech(this, this)

        fetchSpellbeeData()

        speakerIcon.setOnClickListener {
            if (wordList.isNotEmpty()) {
                speakWord(wordList[currentIndex])

            }
        }
        hint_Button.setOnClickListener {
            if (sentenceList.isNotEmpty()) {
                if (!hintUsedForCurrentWord) {
                    hintUsed++
                    hintUsedForCurrentWord = true
                }
                speakSentence(sentenceList[currentIndex])
            }
        }


        submitButton.setOnClickListener {
            val userInput=inputText.text.toString().trim()

            if(userInput.isNotEmpty()){
                val isCorrect=userInput.equals(wordList[currentIndex],ignoreCase = true)
                if (isCorrect){
                    score=score+10
                    scoreButton.text="$score"
                    moveToNextWord()
                    inputText.text.clear()


                    Toast.makeText(this,"Correct",Toast.LENGTH_SHORT).show()
                }else{
                    inputText.text.clear()
                    Toast.makeText(this, "Incorrect, try again!", Toast.LENGTH_SHORT).show()
                    moveToNextWord()
                }
            }
            else{
                Toast.makeText(this,"Please enter the word",Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun moveToNextWord() {
        hintUsedForCurrentWord=false
        currentIndex++
        countDownTimer?.cancel()
       if (currentIndex<wordList.size){
           speakWord(wordList[currentIndex])
           startTimer()
           updateWordCount()
           val progressPercent = ((currentIndex+1).toFloat() / wordList.size.toFloat()) * 100
           updateProgressbar.progress = progressPercent.toInt()
       }
        else{
            Toast.makeText(this,"Game Over and Finial Score is: ${score}",Toast.LENGTH_SHORT).show()
           submitResult()
       }
    }

    private fun updateWordCount() {
        wordCountText.text = "${currentIndex+1}/${wordList.size} words"
    }

    private fun fetchSpellbeeData() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api.getSpellbeeData()
                wordList = response.words
                sentenceList=response.sentences
                startTimer()
                updateWordCount()
            } catch (e: Exception) {
                Log.e("GamePage", "Error: ${e.message}")
                Toast.makeText(this@GamePage, "Failed to load data", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun submitResult() {
        val totalWordsCount = wordList.size
        val correctWordsCount = score / 10
        val mistakeWordsCount = totalWordsCount - correctWordsCount
        val submitDate = java.time.LocalDate.now().toString()
        val hintCount=hintUsed

        val request = SubmitRequest(
            rider_id = 101,
            event_id = "station-s",
            mistake_words = mistakeWordsCount,
            correct_words = correctWordsCount,
            total_points = score,
            submit_date = submitDate
        )

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api.submitResults(request)
                if (response.isSuccessful) {
                    Log.d("Submit", "Data submitted successfully")
                    val intent=Intent(this@GamePage,CongratulationPage::class.java)
                    intent.putExtra("totalWords",totalWordsCount)
                    intent.putExtra("CorrectWords",correctWordsCount)
                    intent.putExtra("MistakeWords",mistakeWordsCount)
                    intent.putExtra("HintUsed",hintCount)
                    startActivity(intent)
                    finish()
                } else {
                    Log.e("Submit", "Submission failed: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("Submit", "Error submitting data: ${e.message}")
            }
        }
    }



    private fun speakWord(word: String) {
        textToSpeech.speak(word, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    private fun speakSentence(sentence: String) {
        textToSpeech.speak(sentence, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    private fun startTimer() {
        countDownTimer = object : CountDownTimer(TIME_PER_WORD, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timerButton.text = "${millisUntilFinished / 1000} sec"
            }

            override fun onFinish() {
                Toast.makeText(this@GamePage, "Time's up!", Toast.LENGTH_SHORT).show()
                moveToNextWord()
            }
        }.start()
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
