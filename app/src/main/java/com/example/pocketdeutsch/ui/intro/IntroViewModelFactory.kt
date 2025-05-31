package com.example.pocketdeutsch.ui.intro

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class IntroViewModelFactory(private val application: Application) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(IntroViewModel::class.java)) {
            return IntroViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}