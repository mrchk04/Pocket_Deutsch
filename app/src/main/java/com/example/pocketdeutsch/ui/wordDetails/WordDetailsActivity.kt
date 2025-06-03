package com.example.pocketdeutsch.ui.wordDetails

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.pocketdeutsch.R
import com.example.pocketdeutsch.data.model.VocabularyItem
import com.example.pocketdeutsch.databinding.ActivityWordDetailsBinding

class WordDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWordDetailsBinding
    private val viewModel: WordDetailsViewModel by viewModels()
    private var vocabularyItem: VocabularyItem? = null

    companion object {
        private const val EXTRA_WORD_ID = "word_id"

        fun newIntent(context: Context, wordId: String): Intent {
            return Intent(context, WordDetailsActivity::class.java).apply {
                putExtra(EXTRA_WORD_ID, wordId)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWordDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Отримуємо дані з Intent
        val wordId = intent.getStringExtra(EXTRA_WORD_ID)

        if (wordId == null) {
            Toast.makeText(this, "Помилка завантаження даних слова", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupViews()
        setupClickListeners()
        observeViewModel()

        viewModel.loadWordById(wordId)
    }

    private fun setupViews() {
        // Налаштування системних барів
        window.statusBarColor = getColor(R.color.white)
        window.navigationBarColor = getColor(R.color.white)
    }

    private fun setupClickListeners() {
        // Кнопка назад
        binding.btnBack.setOnClickListener {
            onBackPressed()
        }

        // Кнопка улюбленого
        binding.favoriteButton.setOnClickListener {
            vocabularyItem?.let { word ->
                viewModel.toggleFavorite(word)
            }
        }
    }

    private fun observeViewModel() {
        // Observer для даних слова
        viewModel.wordData.observe(this) { word ->
            word?.let {
                vocabularyItem = it
                displayWordInfo(it)
            }
        }

        viewModel.isFavorite.observe(this) { isFavorite ->
            updateFavoriteButton(isFavorite)
        }

        viewModel.topicInfo.observe(this) { topic ->
            topic?.let {
                binding.topicInfo.text = "${it.title} (${it.description})"
            }
        }

        viewModel.chapterInfo.observe(this) { chapter ->
            chapter?.let {
                binding.chapterInfo.text = "${it.title} (${it.description})"
            }
        }

        viewModel.error.observe(this) { error ->
            error?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
                viewModel.clearError()
            }
        }
    }

    private fun displayWordInfo(word: VocabularyItem) {
        binding.apply {
            // Основна інформація
            germanWord.text = word.wordDe
            translation.text = word.translation
            partOfSpeech.text = getPartOfSpeechTranslation(word.partOfSpeech)

            // Рід (показуємо тільки для іменників)
            if (word.gender.isNotEmpty() && word.partOfSpeech == "noun") {
                genderLayout.visibility = View.VISIBLE
                gender.text = word.gender
            } else {
                genderLayout.visibility = View.GONE
            }

            // Форма множини
            if (word.pluralForm.isNotEmpty()) {
                pluralLayout.visibility = View.VISIBLE
                pluralForm.text = word.pluralForm
            } else {
                pluralLayout.visibility = View.GONE
            }

            // Приклад речення
            if (word.exampleSentence.isNotEmpty()) {
                exampleLayout.visibility = View.VISIBLE
                exampleSentence.text = word.exampleSentence
            } else {
                exampleLayout.visibility = View.GONE
            }

            // Кнопка аудіо
            audioButton.visibility = if (word.audioUrl.isNotEmpty()) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }
    }

    private fun updateFavoriteButton(isFavorite: Boolean) {
        val iconRes = if (isFavorite) {
            R.drawable.ic_heart_filled
        } else {
            R.drawable.ic_heart_outline
        }
        binding.favoriteButton.setImageResource(iconRes)
    }

    private fun getPartOfSpeechTranslation(partOfSpeech: String): String {
        return when (partOfSpeech.lowercase()) {
            "noun" -> "іменник"
            "verb" -> "дієслово"
            "adjective" -> "прикметник"
            "adverb" -> "прислівник"
            "preposition" -> "прийменник"
            "pronoun" -> "займенник"
            "article" -> "артикль"
            "conjunction" -> "сполучник"
            "interjection" -> "вигук"
            "numeral" -> "числівник"
            else -> partOfSpeech
        }
    }

}