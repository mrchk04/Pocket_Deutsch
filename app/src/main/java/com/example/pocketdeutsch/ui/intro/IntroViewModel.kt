package com.example.pocketdeutsch.ui.intro

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.pocketdeutsch.data.repository.UserRepository
import com.example.pocketdeutsch.utils.PreferencesManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class IntroViewModel(application: Application) : AndroidViewModel(application) {

    private val userRepository = UserRepository()
    private val preferencesManager = PreferencesManager(getApplication())

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private val _navigateToMain = MutableLiveData<Boolean>()
    val navigateToMain: LiveData<Boolean> = _navigateToMain

    private val _navigateToLogin = MutableLiveData<Boolean>()
    val navigateToLogin: LiveData<Boolean> = _navigateToLogin

    private val _userAuthenticated = MutableLiveData<Boolean>()
    val userAuthenticated: LiveData<Boolean> = _userAuthenticated

    fun checkAuthenticationStatus() {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                // Невелика затримка для красивого ефекту
                delay(1000)

                val isAuthenticated = userRepository.isUserAuthenticated()
                _userAuthenticated.value = isAuthenticated

                if (isAuthenticated) {
                    // Перевіряємо, чи є дані користувача
                    val user = userRepository.getCurrentUser()
                    if (user != null) {
                        // Оновлюємо навчальну серію
                        preferencesManager.updateLoginStreak()

                        // Автоматично переходимо на головний екран
                        _navigateToMain.value = true
                    } else {
                        _error.value = "Помилка завантаження даних користувача"
                    }
                }

            } catch (e: Exception) {
                _error.value = "Помилка перевірки авторизації: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun onGetStartedClicked() {
        val isAuthenticated = _userAuthenticated.value ?: false

        if (isAuthenticated) {
            _navigateToMain.value = true
        } else {
            _navigateToLogin.value = true
        }
    }

    fun resetNavigationFlags() {
        _navigateToMain.value = false
        _navigateToLogin.value = false
    }
}