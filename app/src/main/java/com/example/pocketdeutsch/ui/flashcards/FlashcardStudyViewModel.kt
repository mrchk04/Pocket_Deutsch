package com.example.pocketdeutsch.ui.flashcards

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pocketdeutsch.data.model.FlashcardWithVocabulary
import com.example.pocketdeutsch.data.model.StudyProgress
import com.example.pocketdeutsch.data.model.StudyResults
import com.example.pocketdeutsch.data.repository.FlashcardRepository
import kotlinx.coroutines.launch

class FlashcardStudyViewModel(
    private val repository: FlashcardRepository
) : ViewModel() {

    private val _flashcards = MutableLiveData<List<FlashcardWithVocabulary>>()
    val flashcards: LiveData<List<FlashcardWithVocabulary>> = _flashcards

    private val _currentIndex = MutableLiveData<Int>()
    val currentIndex: LiveData<Int> = _currentIndex

    private val _currentFlashcard = MutableLiveData<FlashcardWithVocabulary?>()
    val currentFlashcard: LiveData<FlashcardWithVocabulary?> = _currentFlashcard

    private val _isCardFlipped = MutableLiveData<Boolean>()
    val isCardFlipped: LiveData<Boolean> = _isCardFlipped

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _studyCompleted = MutableLiveData<Boolean>()
    val studyCompleted: LiveData<Boolean> = _studyCompleted

    private val _studyResults = MutableLiveData<StudyResults>()
    val studyResults: LiveData<StudyResults> = _studyResults

    private val _canGoBack = MutableLiveData<Boolean>()
    val canGoBack: LiveData<Boolean> = _canGoBack

    private val _canSkip = MutableLiveData<Boolean>()
    val canSkip: LiveData<Boolean> = _canSkip

    private var completedCards = 0
    private var correctAnswers = 0
    private var studyStartTime = System.currentTimeMillis()

    init {
        _isCardFlipped.value = false
        _canGoBack.value = false
        _canSkip.value = true
    }

    fun loadFlashcards(setId: String, mode: String, startFlashcardId: String? = null) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                studyStartTime = System.currentTimeMillis()

                val flashcards = repository.getStudyFlashcards(setId, mode)
                _flashcards.value = flashcards

                if (flashcards.isNotEmpty()) {
                    // Знаходимо початкову позицію
                    val startIndex = if (startFlashcardId != null) {
                        flashcards.indexOfFirst { it.flashcard.id == startFlashcardId }
                            .takeIf { it >= 0 } ?: 0
                    } else 0

                    setCurrentCard(startIndex)
                } else {
                    _error.value = when (mode) {
                        "new" -> "Немає нових карток для вивчення"
                        "review" -> "Немає карток для повторення"
                        else -> "Немає карток для вивчення"
                    }
                }

            } catch (e: Exception) {
                _error.value = "Помилка завантаження карток: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun flipCard() {
        val currentFlipped = _isCardFlipped.value ?: false
        _isCardFlipped.value = !currentFlipped

        // Якщо картка перевернута, користувач може дати оцінку
        if (!currentFlipped) {
            _canSkip.value = false // Після показу відповіді користувач повинен дати оцінку
        }
    }

    fun answerCard(confidenceLevel: Int) {
        val current = _currentFlashcard.value ?: return
        val isCorrect = confidenceLevel >= 3 // Вважаємо правильною відповідь при рівні впевненості 3+

        viewModelScope.launch {
            try {
                val success = repository.updateFlashcardProgress(
                    flashcardId = current.flashcard.id,
                    confidenceLevel = confidenceLevel,
                    isCorrect = isCorrect
                )

                if (success) {
                    completedCards++
                    if (isCorrect) correctAnswers++

                    // Переходимо до наступної картки
                    nextCard()
                } else {
                    _error.value = "Помилка збереження прогресу"
                }

            } catch (e: Exception) {
                _error.value = "Помилка збереження: ${e.message}"
            }
        }
    }

    private fun nextCard() {
        val currentIdx = _currentIndex.value ?: 0
        val flashcards = _flashcards.value ?: return

        if (currentIdx + 1 < flashcards.size) {
            setCurrentCard(currentIdx + 1)
        } else {
            // Завершення вивчення
            completeStudy()
        }
    }

    private fun setCurrentCard(index: Int) {
        val flashcards = _flashcards.value ?: return

        if (index >= 0 && index < flashcards.size) {
            _currentIndex.value = index
            _currentFlashcard.value = flashcards[index]
            _isCardFlipped.value = false

            // Оновлюємо стан кнопок навігації
            _canGoBack.value = index > 0
            _canSkip.value = true
        }
    }

    private fun completeStudy() {
        viewModelScope.launch {
            try {
                // Оновлюємо статистику користувача
                repository.updateUserStatisticsAfterStudy(completedCards)

                val studyDuration = (System.currentTimeMillis() - studyStartTime) / 1000 // в секундах
                val totalCards = _flashcards.value?.size ?: 0

                val results = StudyResults(
                    totalCards = totalCards,
                    completedCards = completedCards,
                    correctAnswers = correctAnswers,
                    accuracy = if (completedCards > 0) (correctAnswers * 100) / completedCards else 0,
                    studyDuration = studyDuration.toInt()
                )

                _studyResults.value = results
                _studyCompleted.value = true

            } catch (e: Exception) {
                _error.value = "Помилка збереження результатів: ${e.message}"
            }
        }
    }

    fun getCurrentProgress(): StudyProgress {
        val currentIdx = _currentIndex.value ?: 0
        val totalCards = _flashcards.value?.size ?: 0
        val progressPercentage = if (totalCards > 0) ((currentIdx + 1) * 100) / totalCards else 0

        return StudyProgress(
            currentCard = currentIdx + 1,
            totalCards = totalCards,
            progressPercentage = progressPercentage
        )
    }

    fun previousCard() {
        val currentIdx = _currentIndex.value ?: 0
        if (currentIdx > 0) {
            setCurrentCard(currentIdx - 1)
        }
    }

    fun skipCard() {
        // Пропустити можна тільки якщо картка не перевернута
        if (_isCardFlipped.value != true) {
            nextCard()
        }
    }

    fun restartStudy() {
        val flashcards = _flashcards.value ?: return
        if (flashcards.isNotEmpty()) {
            completedCards = 0
            correctAnswers = 0
            studyStartTime = System.currentTimeMillis()
            _studyCompleted.value = false
            setCurrentCard(0)
        }
    }

    fun shuffleCards() {
        val flashcards = _flashcards.value ?: return
        val shuffled = flashcards.shuffled()
        _flashcards.value = shuffled
        setCurrentCard(0)
    }

    fun clearError() {
        _error.value = null
    }

    fun resetCardFlip() {
        _isCardFlipped.value = false
        _canSkip.value = true
    }
}