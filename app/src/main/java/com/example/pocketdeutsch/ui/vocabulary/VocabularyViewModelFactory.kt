package com.example.pocketdeutsch.ui.vocabulary

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class VocabularyViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(VocabularyViewModel::class.java)) {
            return VocabularyViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}