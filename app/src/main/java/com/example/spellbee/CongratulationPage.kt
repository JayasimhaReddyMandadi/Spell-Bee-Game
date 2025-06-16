package com.example.spellbee

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import androidx.core.view.WindowCompat
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.spellbee.data.WordModel
import kotlinx.coroutines.launch

class CongratulationPage : AppCompatActivity() {

    private lateinit var totalWords: TextView
    private lateinit var correctWords: TextView
    private lateinit var wrongWords: TextView
    private lateinit var timer: TextView
    private lateinit var hintUsed: TextView
    private lateinit var playAgainButton: Button
    private lateinit var exitGameButton: Button
    private var isPracticeMode = false
    private var hasPracticed = false
    private var wordModels: ArrayList<WordModel>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_congratulation_page)

        totalWords = findViewById(R.id.totalWords)
        correctWords = findViewById(R.id.correctWords)
        wrongWords = findViewById(R.id.wrongWords)
        timer = findViewById(R.id.timer)
        hintUsed = findViewById(R.id.hintUsed)
        playAgainButton = findViewById(R.id.playAgainButton)
        exitGameButton = findViewById(R.id.exitGameButton)

        // Get data from intent
        val totalWordsCount = intent.getIntExtra("totalWords", 0)
        val correctWordsCount = intent.getIntExtra("CorrectWords", 0)
        val mistakeWordsCount = intent.getIntExtra("MistakeWords", 0)
        val hintUsedCount = intent.getIntExtra("HintUsed", 0)
        val totalTime = intent.getIntExtra("TotalTime", 0)
        isPracticeMode = intent.getBooleanExtra("isPracticeMode", false)
        hasPracticed = intent.getBooleanExtra("hasPracticed", false)
        wordModels = intent.getParcelableArrayListExtra("wordModels")

        // Update UI with the data
        totalWords.text = "$totalWordsCount"
        correctWords.text = "$correctWordsCount"
        wrongWords.text = "$mistakeWordsCount"
        hintUsed.text = "$hintUsedCount"
        timer.text = "$totalTime seconds"

        // Show replay popup if there are incorrect or skipped words
        if (!isPracticeMode && wordModels != null && wordModels!!.isNotEmpty()) {
            var hasIncorrectOrSkipped = false
            for (word in wordModels!!) {
                if (word.isIncorrect || word.isSkipped) {
                    hasIncorrectOrSkipped = true
                    break
                }
            }
            if (hasIncorrectOrSkipped) {
                showReplayPopup(wordModels!!, mistakeWordsCount)
            }
        }

        playAgainButton.setOnClickListener {
            if (isPracticeMode && !hasPracticed && mistakeWordsCount > 0) {
                // Show replay popup for practice mode with mistakes
                showReplayPopup(wordModels ?: arrayListOf(), mistakeWordsCount)
            } else {
                // Regular play again behavior
                val intent = if (isPracticeMode) {
                    Intent(this@CongratulationPage, PracticePage::class.java)
                } else {
                    Intent(this@CongratulationPage, GamePage::class.java)
                }
                startActivity(intent)
                finish()
            }
        }

        exitGameButton.setOnClickListener {
            val intent = Intent(this@CongratulationPage, LandingPage::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun showReplayPopup(wordModels: ArrayList<WordModel>, totalWordsToPractice: Int) {
        var incorrectCount = 0
        var skippedCount = 0

        // Filter only incorrect and skipped words
        val practiceWords = wordModels.filter { it.isIncorrect || it.isSkipped }

        for (word in practiceWords) {
            if (word.isIncorrect) incorrectCount++
            if (word.isSkipped) skippedCount++
        }

        val builder = AlertDialog.Builder(this)
        val customView = layoutInflater.inflate(R.layout.dialog_practice, null)
        builder.setView(customView)

        val dialogMessage = customView.findViewById<TextView>(R.id.dialogMessage)
        val btnYes = customView.findViewById<Button>(R.id.btnYes)
        val btnNo = customView.findViewById<Button>(R.id.btnNo)

        dialogMessage.text = "You have made $incorrectCount mistakes and skipped $skippedCount words. Would you like to practice these words to improve your accuracy?"

        val dialog = builder.create()
        dialog.setCancelable(false)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()

        btnYes.setOnClickListener {
            val intent = Intent(this@CongratulationPage, PracticePage::class.java)
            intent.putParcelableArrayListExtra("wordModels", ArrayList(practiceWords))
            intent.putExtra("isPracticeMode", true)
            intent.putExtra("hasPracticed", true)
            intent.putExtra("totalWords", incorrectCount + skippedCount)
            intent.putExtra("useCustomWords", true) // Add flag to indicate we're using custom words
            startActivity(intent)
            finish()
        }

        btnNo.setOnClickListener {
            dialog.dismiss()
        }
    }
}

