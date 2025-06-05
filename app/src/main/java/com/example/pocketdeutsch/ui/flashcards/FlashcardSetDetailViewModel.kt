package com.example.pocketdeutsch.ui.flashcards

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pocketdeutsch.data.model.*
import com.example.pocketdeutsch.data.repository.FlashcardRepository
import com.example.pocketdeutsch.data.model.FlashcardSetStatistics
import com.example.pocketdeutsch.data.repository.UserRepository
import kotlinx.coroutines.launch

class FlashcardSetDetailViewModel : ViewModel() {
    private val repository = FlashcardRepository()
    private val userRepository = UserRepository()

    private val _userNotAuthenticated = MutableLiveData<Boolean>()
    val userNotAuthenticated: LiveData<Boolean> = _userNotAuthenticated

    private val _userData = MutableLiveData<User?>()
    val userData: LiveData<User?> = _userData

    private val _flashcardSet = MutableLiveData<FlashcardSet?>()
    val flashcardSet: LiveData<FlashcardSet?> = _flashcardSet

    private val _topic = MutableLiveData<Topic?>()
    val topic: LiveData<Topic?> = _topic

    private val _flashcards = MutableLiveData<List<FlashcardWithVocabulary>>()
    val flashcards: LiveData<List<FlashcardWithVocabulary>> = _flashcards

    private val _statistics = MutableLiveData<FlashcardSetStatistics>()
    val statistics: LiveData<FlashcardSetStatistics> = _statistics

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun loadFlashcardSet(setId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                // Завантажуємо основну інформацію про набір
                val flashcardSet = repository.getFlashcardSetById(setId)
                _flashcardSet.value = flashcardSet

                if (flashcardSet != null) {
                    // Завантажуємо тему
                    if (flashcardSet.topicId != null) {
                        val topic = repository.getTopic(flashcardSet.topicId.toString())
                        _topic.value = topic
                    }

                    // Завантажуємо флеш-картки зі словниковими одиницями
                    val flashcards = repository.getFlashcardsWithVocabulary(setId)
                    _flashcards.value = flashcards

                    // Завантажуємо статистику
                    val statistics = repository.getSetStatistics(setId)
                    _statistics.value = statistics
                }

            } catch (e: Exception) {
                _error.value = "Помилка завантаження даних: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refreshData(setId: String) {
        loadFlashcardSet(setId)
    }

    fun getNewFlashcards(): List<FlashcardWithVocabulary> {
        return _flashcards.value?.filter {
            it.progress == null || it.progress.attempts == 0
        } ?: emptyList()
    }

    fun getStudiedFlashcards(): List<FlashcardWithVocabulary> {
        return _flashcards.value?.filter {
            it.progress != null && it.progress.attempts > 0
        } ?: emptyList()
    }

    fun getAllFlashcards(): List<FlashcardWithVocabulary> {
        return _flashcards.value ?: emptyList()
    }

    fun clearError() {
        _error.value = null
    }

    fun loadUserData() {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                if (!userRepository.isUserAuthenticated()) {
                    _userNotAuthenticated.value = true
                    return@launch
                }

                val user = userRepository.getCurrentUser()
                if (user != null) {
                    _userData.value = user
                } else {
                    _error.value = "Неможливо завантажити дані користувача."
                }

            } catch (e: Exception) {
                _error.value = "Помилка під час завантаження даних користувача: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}