package com.example.spellbee

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeech.OnInitListener
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.spellbee.api.RetrofitClient
import com.example.spellbee.data.PracticeWord
import com.example.spellbee.data.WordModel
import kotlinx.coroutines.launch
import java.util.*

class PracticePage : AppCompatActivity(), OnInitListener {

    private lateinit var speakerIcon: ImageView
    private lateinit var inputText: EditText
    private lateinit var submitButton: Button
    private lateinit var scoreButton: TextView
    private lateinit var hint_Button: LinearLayout
    private lateinit var wordCountText: TextView
    private lateinit var backIcon: ImageView
    private lateinit var updateProgressbar: ProgressBar
    private lateinit var timerButton: TextView
    private lateinit var textToSpeech: TextToSpeech
    private var practiceWords: List<PracticeWord> = listOf()
    private var currentIndex = 0
    private var score = 0
    private var hintUsed = 0
    private var hintUsedForCurrentWord = false
    private var mistakeWordsCount = 0
    private var correctWordsCount = 0
    private var startTime: Long = 0
    private var currentWordStartTime: Long = 0
    private var timerHandler: Handler? = null
    private var timerRunnable: Runnable? = null
    private var seconds = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_game_page)

        val titleText = findViewById<TextView>(R.id.text1)
        titleText.text = "Practice Mode"
        titleText.requestFocus()
        titleText.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED)

        speakerIcon = findViewById(R.id.speakerIcon)
        inputText = findViewById(R.id.inputText)
        submitButton = findViewById(R.id.submitButton)
        scoreButton = findViewById(R.id.scoreButton)
        hint_Button = findViewById(R.id.hint_Button)
        wordCountText = findViewById(R.id.wordCountText)
        updateProgressbar = findViewById(R.id.updateProgressbar)
        backIcon = findViewById(R.id.backIcon)
        timerButton = findViewById(R.id.timerButton)

        textToSpeech = TextToSpeech(this, this)
        startTime = System.currentTimeMillis()
        currentWordStartTime = startTime

        // Check if we're using custom words from GamePage
        val useCustomWords = intent.getBooleanExtra("useCustomWords", false)
        if (useCustomWords) {
            val wordModels = intent.getParcelableArrayListExtra<WordModel>("wordModels")
            if (wordModels != null) {
                // Convert WordModel list to PracticeWord list
                practiceWords = wordModels.map { PracticeWord(it.word, it.sentence) }
                if (practiceWords.isNotEmpty()) {
                    showHowToPlayDialog()
                }
            }
        } else {
            fetchPracticeWords()
        }

        speakerIcon.setOnClickListener {
            if (practiceWords.isNotEmpty()) {
                speakWord(practiceWords[currentIndex].word)
            }
        }

        hint_Button.setOnClickListener {
            if (practiceWords.isNotEmpty()) {
                if (!hintUsedForCurrentWord) {
                    hintUsed++
                    hintUsedForCurrentWord = true
                }
                speakSentence(practiceWords[currentIndex].sentence)
            }
        }

        backIcon.setOnClickListener { showExitConfirmationDialog() }

        submitButton.setOnClickListener {
            val userInput = inputText.text.toString().trim()
            if (userInput.isEmpty()) {
                Toast.makeText(this, "Please enter a word", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (practiceWords.isNotEmpty()) {
                val currentWord = practiceWords[currentIndex].word
                val isCorrect = userInput.equals(currentWord, ignoreCase = true)
                inputText.text.clear()
                if (isCorrect) {
                    score++
                    scoreButton.text = "Score: $score"
                } else {
                    mistakeWordsCount++
                }
                showResultDialog(isCorrect, currentWord)
            }
        }
    }

    private fun showHowToPlayDialog() {
        val dialogView = layoutInflater.inflate(R.layout.how_to_play_dialog, null)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        val proceedButton = dialogView.findViewById<Button>(R.id.proceed_button)
        proceedButton.setOnClickListener {
            dialog.dismiss()

            if (practiceWords.isNotEmpty()) {
                currentIndex = 0
                startTimer()
                updateWordCount()
                val progressPercent = ((currentIndex + 1).toFloat() / practiceWords.size.toFloat()) * 100
                updateProgressbar.progress = progressPercent.toInt()
            }
        }

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }

    private fun startTimer() {
        seconds = 0
        timerHandler = Handler(Looper.getMainLooper())
        timerRunnable = object : Runnable {
            override fun run() {
                seconds++
                timerButton.text = "$seconds sec"
                timerHandler?.postDelayed(this, 1000)
            }
        }
        timerHandler?.post(timerRunnable!!)
    }

    private fun stopTimer() {
        timerHandler?.removeCallbacks(timerRunnable!!)
    }

    private fun fetchPracticeWords() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api.getSpellbeePracticeData()
                practiceWords = response.words
                if (practiceWords.isNotEmpty()) {
                    showHowToPlayDialog()
                }
            } catch (e: Exception) {
                Log.e("PracticePage", "Error fetching practice words: ${e.message}")
                Toast.makeText(this@PracticePage, "Error loading practice words", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showResultDialog(isCorrect: Boolean, correctWord: String) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_result, null)
        val resultText = dialogView.findViewById<TextView>(R.id.result_text)
        val nextButton = dialogView.findViewById<Button>(R.id.next_button)
        stopTimer()

        if (isCorrect) {
            resultText.text = "Great job! You spelled the word correctly."
        } else {
            resultText.text = "Oops! The correct spelling is: $correctWord"
        }

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        nextButton.setOnClickListener {
            dialog.dismiss()
            moveToNextWord()
        }

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }

    private fun moveToNextWord() {
        currentIndex++
        inputText.text.clear()
        hintUsedForCurrentWord = false
        
        if (currentIndex < practiceWords.size) {
            updateWordCount()
            val progressPercent = ((currentIndex + 1).toFloat() / practiceWords.size.toFloat()) * 100
            updateProgressbar.progress = progressPercent.toInt()
            startTimer() // Start timer for the new word
        } else {
            // All words completed, show congratulation page
            val totalTimeSeconds = ((System.currentTimeMillis() - startTime) / 1000).toInt()
            val intent = Intent(this@PracticePage, CongratulationPage::class.java)
            intent.putExtra("totalWords", practiceWords.size)
            intent.putExtra("CorrectWords", score)
            intent.putExtra("MistakeWords", mistakeWordsCount)
            intent.putExtra("HintUsed", hintUsed)
            intent.putExtra("TotalTime", totalTimeSeconds)
            intent.putExtra("isPracticeMode", true)
            startActivity(intent)
            finish()
        }
    }

    private fun updateWordCount() {
        wordCountText.text = "${currentIndex + 1}/${practiceWords.size} words"
    }

    private fun speakWord(word: String) {
        textToSpeech.speak(word, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    private fun speakSentence(sentence: String) {
        textToSpeech.speak(sentence, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    private fun showExitConfirmationDialog() {
        stopTimer()
        val dialogView = layoutInflater.inflate(R.layout.dialog_exit_confirmation, null)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        val cancelButton = dialogView.findViewById<Button>(R.id.cancel_button)
        val yesButton = dialogView.findViewById<Button>(R.id.yes_button)

        cancelButton.setOnClickListener {
            dialog.dismiss()
            startTimer() // Resume timer if user cancels exit
        }

        yesButton.setOnClickListener {
            dialog.dismiss()
            val intent = Intent(this@PracticePage, LandingPage::class.java)
            startActivity(intent)
            finish()
        }

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = textToSpeech.setLanguage(Locale.US)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "Language not supported")
            }
        } else {
            Log.e("TTS", "Initialization failed")
        }
    }

    override fun onDestroy() {
        stopTimer()
        if (::textToSpeech.isInitialized) {
            textToSpeech.stop()
            textToSpeech.shutdown()
        }
        super.onDestroy()
    }
}