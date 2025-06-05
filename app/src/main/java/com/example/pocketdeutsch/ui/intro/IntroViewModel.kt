package com.example.pocketdeutsch.ui.intro

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth

class IntroViewModel(application: Application) : AndroidViewModel(application) {

    private val auth = FirebaseAuth.getInstance()

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private val _navigateToMain = MutableLiveData<Boolean>()
    val navigateToMain: LiveData<Boolean> = _navigateToMain

    private val _navigateToLogin = MutableLiveData<Boolean>()
    val navigateToLogin: LiveData<Boolean> = _navigateToLogin

    fun checkAuthenticationStatus() {
        _isLoading.value = true

        val currentUser = auth.currentUser

        if (currentUser != null) {
            // Користувач авторизований, переходимо до MainActivity
            _navigateToMain.value = true
        }
        // Якщо користувач не авторизований, залишаємось на IntroActivity

        _isLoading.value = false
    }

    fun onGetStartedClicked() {
        // Просто переходимо до LoginActivity
        _navigateToLogin.value = true
    }

    // Методи для скидання навігаційних станів
    fun onNavigatedToMain() {
        _navigateToMain.value = false
    }

    fun onNavigatedToLogin() {
        _navigateToLogin.value = false
    }
}