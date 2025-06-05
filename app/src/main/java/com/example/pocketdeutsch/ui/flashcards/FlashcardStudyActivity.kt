package com.example.pocketdeutsch.ui.flashcards

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.pocketdeutsch.R
import com.example.pocketdeutsch.data.model.FlashcardWithVocabulary
import com.example.pocketdeutsch.data.model.StudyResults
import com.example.pocketdeutsch.data.model.VocabularyItem
import com.example.pocketdeutsch.data.repository.FlashcardRepository
import com.example.pocketdeutsch.databinding.ActivityFlashcardStudyBinding
import kotlinx.coroutines.launch

class FlashcardStudyActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFlashcardStudyBinding
    private lateinit var viewModel: FlashcardStudyViewModel

    private var flashcardSetId: String = ""
    private var flashcardSetTitle: String = ""
    private var studyMode: String = ""
    private var startFlashcardId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFlashcardStudyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        getIntentData()
        initializeComponents()
        setupClickListeners()
        observeData()

        // Завантажуємо флеш-картки
        viewModel.loadFlashcards(flashcardSetId, studyMode, startFlashcardId)
    }

    private fun getIntentData() {
        flashcardSetId = intent.getStringExtra("flashcard_set_id") ?: ""
        flashcardSetTitle = intent.getStringExtra("flashcard_set_title") ?: ""
        studyMode = intent.getStringExtra("study_mode") ?: "all"
        startFlashcardId = intent.getStringExtra("start_flashcard_id")

        if (flashcardSetId.isEmpty()) {
            Toast.makeText(this, "Помилка: не вказано ID набору", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
    }

    private fun initializeComponents() {
        val repository = FlashcardRepository()
        val factory = FlashcardViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[FlashcardStudyViewModel::class.java]
    }

    private fun setupClickListeners() {
        binding.apply {
            // Закриття активності
            btnClose.setOnClickListener {
                showExitConfirmation()
            }

            // Налаштування (можна додати пізніше)
            btnSettings.setOnClickListener {
                showStudySettingsDialog()
            }

            // Натискання на картку для перевороту
            flashcardContainer.setOnClickListener {
                if (viewModel.isCardFlipped.value != true) {
                    viewModel.flipCard()
                }
            }

            // Кнопка "Показати відповідь"
//            btnShowAnswer.setOnClickListener {
//                viewModel.flipCard()
//            }

            // Кнопки оцінки
            btnAgain.setOnClickListener {
                viewModel.answerCard(1)
            }
            btnHard.setOnClickListener {
                viewModel.answerCard(2)
            }
            btnGood.setOnClickListener {
                viewModel.answerCard(3)
            }
            btnEasy.setOnClickListener {
                viewModel.answerCard(4)
            }
        }
    }

    private fun observeData() {
        // Поточна флеш-картка
        viewModel.currentFlashcard.observe(this) { flashcard ->
            flashcard?.let { updateFlashcardDisplay(it) }
        }

        // Стан перевороту картки
        viewModel.isCardFlipped.observe(this) { isFlipped ->
            updateCardSides(isFlipped)
        }

        // Прогрес вивчення
        viewModel.currentIndex.observe(this) {
            updateProgress()
        }

        // Стан завантаження
        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        // Помилки
        viewModel.error.observe(this) { error ->
            error?.let {
                showError(it)
                viewModel.clearError()
            }
        }

        // Завершення вивчення
        viewModel.studyCompleted.observe(this) { completed ->
            if (completed) {
                // Результати автоматично покажуться через studyResults observer
            }
        }

        // Результати вивчення
        viewModel.studyResults.observe(this) { results ->
            results?.let { showResultsDialog(it) }
        }

        // Можливість повернутися назад
        viewModel.canGoBack.observe(this) { canGoBack ->
            // Можна додати кнопку "Назад" пізніше
        }

        // Можливість пропустити картку
        viewModel.canSkip.observe(this) { canSkip ->
            // Можна додати кнопку "Пропустити" пізніше
        }
    }

    private fun updateFlashcardDisplay(flashcard: FlashcardWithVocabulary) {
        binding.apply {
            // Оновлюємо заголовок
            studyTitle.text = "$flashcardSetTitle"

            // Фронтальна сторона (німецьке слово)
            germanWord.text = formatGermanWord(flashcard.vocabularyItem)
            partOfSpeech.text = flashcard.vocabularyItem.partOfSpeech

            // Зворотна сторона (переклад)
            germanWordBack.text = formatGermanWord(flashcard.vocabularyItem)
            translation.text = flashcard.vocabularyItem.translation

            // Додаткова інформація
            updateAdditionalInfo(flashcard.vocabularyItem)
        }
    }

    private fun formatGermanWord(vocabularyItem: VocabularyItem): String {
        return when {
            vocabularyItem.gender.isNotEmpty() -> "${vocabularyItem.wordDe}"
            else -> vocabularyItem.wordDe
        }
    }

    private fun updateAdditionalInfo(vocabularyItem: VocabularyItem) {
        binding.apply {
            // Форма множини
            if (vocabularyItem.pluralForm.isNotEmpty()) {
                pluralForm.text = vocabularyItem.pluralForm
                pluralForm.visibility = View.VISIBLE
            } else {
                pluralForm.visibility = View.GONE
            }

            // Приклад речення
            if (vocabularyItem.exampleSentence.isNotEmpty()) {
                exampleSentence.text = vocabularyItem.exampleSentence
                exampleSentence.visibility = View.VISIBLE
            } else {
                exampleSentence.visibility = View.GONE
            }
        }
    }

    private fun updateCardSides(isFlipped: Boolean) {
        binding.apply {
            if (isFlipped) {
                // Показуємо зворотну сторону з анімацією
                animateCardFlip(true)

                // Показуємо кнопки оцінки, приховуємо кнопку "Показати відповідь"
                ratingButtons.visibility = View.VISIBLE
//                btnShowAnswer.visibility = View.GONE
            } else {
                // Показуємо фронтальну сторону
                animateCardFlip(false)

                // Приховуємо кнопки оцінки, показуємо кнопку "Показати відповідь"
                ratingButtons.visibility = View.GONE
//                btnShowAnswer.visibility = View.VISIBLE
            }
        }
    }

    private fun animateCardFlip(showBack: Boolean) {
        val frontView = binding.flashcardFront
        val backView = binding.flashcardBack

        if (showBack) {
            frontView.visibility = View.GONE
            backView.visibility = View.VISIBLE
        } else {
            frontView.visibility = View.VISIBLE
            backView.visibility = View.GONE
        }

        // Тут можна додати анімацію перевороту картки
    }

    private fun updateProgress() {
        val progress = viewModel.getCurrentProgress()
        binding.apply {
            progressText.text = "${progress.currentCard} з ${progress.totalCards}"
            progressBar.progress = progress.progressPercentage
        }
    }


    private fun showStudySettingsDialog() {
        val options = arrayOf(
            "Перемішати картки",
            "Почати спочатку",
            "Змінити режим вивчення"
        )

        AlertDialog.Builder(this)
            .setTitle("Налаштування вивчення")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> viewModel.shuffleCards()
                    1 -> showRestartConfirmation()
                    2 -> showModeSelectionDialog()
                }
            }
            .show()
    }

    private fun showRestartConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Почати спочатку?")
            .setMessage("Поточний прогрес буде скинуто")
            .setPositiveButton("Так") { _, _ ->
                viewModel.restartStudy()
            }
            .setNegativeButton("Скасувати", null)
            .show()
    }

    private fun showModeSelectionDialog() {
        val modes = arrayOf("Всі картки", "Тільки нові", "Повторення")
        val modeValues = arrayOf("all", "new", "review")

        AlertDialog.Builder(this)
            .setTitle("Режим вивчення")
            .setItems(modes) { _, which ->
                val newMode = modeValues[which]
                if (newMode != studyMode) {
                    studyMode = newMode
                    viewModel.loadFlashcards(flashcardSetId, studyMode)
                }
            }
            .show()
    }

    private fun showExitConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Завершити вивчення?")
            .setMessage("Ваш прогрес буде збережено")
            .setPositiveButton("Так") { _, _ ->
                setResult(RESULT_OK)
                finish()
            }
            .setNegativeButton("Скасувати", null)
            .show()
    }

    private fun showResultsDialog(results: StudyResults) {
        val studyTime = formatStudyTime(results.studyDuration)
        val message = """
            Вивчення завершено! 🎉
            
            📊 Статистика:
            • Карток вивчено: ${results.completedCards} з ${results.totalCards}
            • Правильних відповідей: ${results.correctAnswers}
            • Точність: ${results.accuracy}%
            • Час вивчення: $studyTime
            
            ${getMotivationalMessage(results.accuracy)}
        """.trimIndent()

        AlertDialog.Builder(this)
            .setTitle("Результати вивчення")
            .setMessage(message)
            .setPositiveButton("Готово") { _, _ ->
                setResult(RESULT_OK)
                finish()
            }
            .setNeutralButton("Ще раз") { _, _ ->
                viewModel.restartStudy()
            }
            .setCancelable(false)
            .show()
    }

    private fun formatStudyTime(seconds: Int): String {
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return when {
            minutes > 0 -> "${minutes}хв ${remainingSeconds}с"
            else -> "${seconds}с"
        }
    }

    private fun getMotivationalMessage(accuracy: Int): String {
        return when {
            accuracy >= 90 -> "Відмінний результат! 🌟"
            accuracy >= 75 -> "Дуже добре! 👍"
            accuracy >= 60 -> "Непогано, продовжуйте! 💪"
            else -> "Потрібно більше практики! 📚"
        }
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    override fun onBackPressed() {
        showExitConfirmation()
    }

        companion object {
        const val EXTRA_FLASHCARD_SET_ID = "flashcard_set_id"
        const val EXTRA_FLASHCARD_SET_TITLE = "flashcard_set_title"
        const val EXTRA_STUDY_MODE = "study_mode"
        const val EXTRA_START_FLASHCARD_ID = "start_flashcard_id"

        fun createIntent(
            context: android.content.Context,
            setId: String,
            setTitle: String,
            mode: String = "all",
            startFlashcardId: String? = null
        ): Intent {
            return Intent(context, FlashcardStudyActivity::class.java).apply {
                putExtra(EXTRA_FLASHCARD_SET_ID, setId)
                putExtra(EXTRA_FLASHCARD_SET_TITLE, setTitle)
                putExtra(EXTRA_STUDY_MODE, mode)
                startFlashcardId?.let { putExtra(EXTRA_START_FLASHCARD_ID, it) }
            }
        }
    }
}