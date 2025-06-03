package com.example.pocketdeutsch.utils

import android.content.Context
import android.content.SharedPreferences
import com.example.pocketdeutsch.data.model.DataHealthStatus
import com.example.pocketdeutsch.data.repository.VocabularyRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DataInitializationManager(private val context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        "app_initialization",
        Context.MODE_PRIVATE
    )

    private val vocabularyRepository = VocabularyRepository(context)

    companion object {
        private const val KEY_DATA_INITIALIZED = "data_initialized"
        private const val KEY_INITIALIZATION_VERSION = "initialization_version"
        private const val CURRENT_VERSION = 1
    }

    suspend fun initializeAppData(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                if (isDataInitialized()) {
                    return@withContext true
                }

                // Перевіряємо, чи дані вже завантажені в Firestore
                val existingWords = vocabularyRepository.getAllWords()

                if (existingWords.isNotEmpty()) {
                    // Дані вже є в Firestore, позначаємо як ініціалізовані
                    markAsInitialized()
                    return@withContext true
                }

                // Якщо даних немає, це означає що треба їх імпортувати через Node.js скрипт
                false

            } catch (e: Exception) {
                false
            }
        }
    }

    private fun isDataInitialized(): Boolean {
        val isInitialized = prefs.getBoolean(KEY_DATA_INITIALIZED, false)
        val version = prefs.getInt(KEY_INITIALIZATION_VERSION, 0)

        return isInitialized && version >= CURRENT_VERSION
    }

    private fun markAsInitialized() {
        prefs.edit()
            .putBoolean(KEY_DATA_INITIALIZED, true)
            .putInt(KEY_INITIALIZATION_VERSION, CURRENT_VERSION)
            .apply()
    }

    fun resetInitialization() {
        prefs.edit()
            .putBoolean(KEY_DATA_INITIALIZED, false)
            .putInt(KEY_INITIALIZATION_VERSION, 0)
            .apply()
    }

    suspend fun checkDataHealth(): DataHealthStatus {
        return withContext(Dispatchers.IO) {
            try {
                val words = vocabularyRepository.getAllWords()
                val topics = vocabularyRepository.getAllTopics()
                val chapters = vocabularyRepository.getAllChapters()
                val textbooks = vocabularyRepository.getAllTextbooks()

                DataHealthStatus(
                    wordsCount = words.size,
                    topicsCount = topics.size,
                    chaptersCount = chapters.size,
                    textbooksCount = textbooks.size,
                    isHealthy = words.size >= 100 // Мінімум 100 слів для здорового стану
                )
            } catch (e: Exception) {
                DataHealthStatus(
                    wordsCount = 0,
                    topicsCount = 0,
                    chaptersCount = 0,
                    textbooksCount = 0,
                    isHealthy = false,
                    error = e.message
                )
            }
        }
    }
}

