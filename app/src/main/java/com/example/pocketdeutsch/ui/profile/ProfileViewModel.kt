package com.example.pocketdeutsch.ui.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.pocketdeutsch.data.model.User
import com.example.pocketdeutsch.data.model.UserStatistics
import com.example.pocketdeutsch.data.repository.UserRepository
import com.example.pocketdeutsch.data.repository.StatisticsRepository
import com.example.pocketdeutsch.utils.PreferencesManager
import kotlinx.coroutines.launch

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val userRepository = UserRepository()
    private val statisticsRepository = StatisticsRepository(getApplication())
    private val preferencesManager = PreferencesManager(getApplication())

    private val _userData = MutableLiveData<User?>()
    val userData: LiveData<User?> = _userData

    private val _userStatistics = MutableLiveData<UserStatistics?>()
    val userStatistics: LiveData<UserStatistics?> = _userStatistics

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private val _logoutSuccess = MutableLiveData<Boolean>()
    val logoutSuccess: LiveData<Boolean> = _logoutSuccess

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
                    _error.value = "Не вдалося завантажити дані користувача"
                }

            } catch (e: Exception) {
                _error.value = "Помилка при завантаженні даних: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadUserStatistics() {
        viewModelScope.launch {
            try {
                if (!userRepository.isUserAuthenticated()) {
                    return@launch
                }

                val statistics = statisticsRepository.getUserStatistics()
                _userStatistics.value = statistics

            } catch (e: Exception) {
                _error.value = "Помилка при завантаженні статистики: ${e.message}"
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                // Очищаємо локальні дані
                preferencesManager.clearAllData()

                // Виходимо з Firebase Auth
                userRepository.signOut()

                _logoutSuccess.value = true

            } catch (e: Exception) {
                _error.value = "Помилка при виході з системи: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refreshData() {
        loadUserData()
        loadUserStatistics()
    }
}