package com.example.pocketdeutsch.ui.flashcards

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.pocketdeutsch.data.repository.FlashcardRepository

class FlashcardViewModelFactory(
    private val repository: FlashcardRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(FlashcardSetsViewModel::class.java) -> {
                // Поточні ViewModel не приймають параметри в конструкторі
                // Для кастомної логіки треба було б змінити архітектуру
                FlashcardSetsViewModel() as T
            }
            modelClass.isAssignableFrom(FlashcardSetDetailViewModel::class.java) -> {
                FlashcardSetDetailViewModel() as T
            }
            modelClass.isAssignableFrom(FlashcardStudyViewModel::class.java) -> {
                FlashcardStudyViewModel(repository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}