package com.example.pocketdeutsch.ui.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.pocketdeutsch.data.model.User
import com.example.pocketdeutsch.data.model.VocabularyItem
import com.example.pocketdeutsch.data.repository.UserRepository
import com.example.pocketdeutsch.data.repository.VocabularyRepository
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val userRepository = UserRepository()
    private val vocabularyRepository = VocabularyRepository(getApplication())

    private val _userData = MutableLiveData<User?>()
    val userData: LiveData<User?> = _userData

    private val _wordOfTheDay = MutableLiveData<VocabularyItem?>()
    val wordOfTheDay: LiveData<VocabularyItem?> = _wordOfTheDay

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private val _userNotAuthenticated = MutableLiveData<Boolean>()
    val userNotAuthenticated: LiveData<Boolean> = _userNotAuthenticated

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

    fun loadWordOfTheDay() {
        viewModelScope.launch {
            try {
                val wordOfDay = vocabularyRepository.getWordOfTheDay()
                if (wordOfDay != null) {
                    _wordOfTheDay.value = wordOfDay
                } else {
                    _error.value = "Не вдалося завантажити слово дня"
                }
            } catch (e: Exception) {
                _error.value = "Помилка під час завантаження слова дня: ${e.message}"
            }
        }
    }
}