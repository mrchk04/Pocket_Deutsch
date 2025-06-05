package com.example.pocketdeutsch.ui.flashcards

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pocketdeutsch.data.model.FlashcardSetWithProgress
import com.example.pocketdeutsch.data.model.User
import com.example.pocketdeutsch.data.model.UserStatistics
import com.example.pocketdeutsch.data.repository.FlashcardRepository
import com.example.pocketdeutsch.data.repository.UserRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.async

class FlashcardSetsViewModel : ViewModel() {
    private val repository = FlashcardRepository()
    private val userRepository = UserRepository()

    private val _userNotAuthenticated = MutableLiveData<Boolean>()
    val userNotAuthenticated: LiveData<Boolean> = _userNotAuthenticated

    private val _flashcardSets = MutableLiveData<List<FlashcardSetWithProgress>>()
    val flashcardSets: LiveData<List<FlashcardSetWithProgress>> = _flashcardSets

    private val _userStatistics = MutableLiveData<UserStatistics>()
    val userStatistics: LiveData<UserStatistics> = _userStatistics

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _userData = MutableLiveData<User?>()
    val userData: LiveData<User?> = _userData

    private var isDataLoaded = false

    init {
        loadData()
    }

    fun loadData() {
        // Не перезавантажуємо дані, якщо вони вже завантажені
        if (isDataLoaded && !_flashcardSets.value.isNullOrEmpty()) {
            return
        }

        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                // Завантажуємо набори флеш-карток та статистику паралельно
                val setsDeferred = async { repository.getFlashcardSetsWithProgress() }
                val statsDeferred = async { repository.getUserStatistics() }

                val sets = setsDeferred.await()
                val stats = statsDeferred.await()

                _flashcardSets.value = sets
                _userStatistics.value = stats
                isDataLoaded = true

            } catch (e: Exception) {
                _error.value = "Помилка завантаження даних: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refreshData() {
        isDataLoaded = false
        loadData()
    }

    fun updateStatistics() {
        viewModelScope.launch {
            try {
                repository.updateUserStatistics()
                val stats = repository.getUserStatistics()
                _userStatistics.value = stats
            } catch (e: Exception) {
                // Логування помилки
            }
        }
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