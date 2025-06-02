package com.example.pocketdeutsch.data.repository

import android.content.Context
import com.example.pocketdeutsch.data.model.UserStatistics
import com.example.pocketdeutsch.utils.DateUtils
import com.example.pocketdeutsch.utils.PreferencesManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class StatisticsRepository(private val context: Context) {

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val statisticsCollection = firestore.collection("user_statistics")
    private val userVocabularyCollection = firestore.collection("user_vocabulary")
    private val userFlashcardProgressCollection = firestore.collection("user_flashcard_progress")
    private val preferencesManager = PreferencesManager(context)

    suspend fun getUserStatistics(): UserStatistics? {
        try {
            val currentUser = firebaseAuth.currentUser ?: return null
            val userId = currentUser.uid

            // Спробуємо отримати статистику з Firestore
            val document = statisticsCollection.document(userId).get().await()

            return if (document.exists()) {
                // Оновлюємо статистику
                val existingStats = document.toObject(UserStatistics::class.java)?.copy(userId = userId)
                val updatedStats = calculateUserStatistics(existingStats)

                // Зберігаємо оновлену статистику
                saveUserStatistics(updatedStats)
                updatedStats
            } else {
                // Створюємо нову статистику
                val newStats = calculateUserStatistics(null)
                saveUserStatistics(newStats)
                newStats
            }

        } catch (e: Exception) {
            throw e
        }
    }

    private suspend fun calculateUserStatistics(existingStats: UserStatistics?): UserStatistics {
        val currentUser = firebaseAuth.currentUser ?: throw IllegalStateException("User not authenticated")
        val userId = currentUser.uid

        try {
            // Підрахунок вивчених слів (з user_vocabulary)
            val learnedWordsCount = getUserVocabularyCount(userId)

            // Підрахунок улюблених слів
            val favoriteWordsCount = getFavoriteWordsCount(userId)

            // Підрахунок виконаних флеш-карток
            val flashcardsCompleted = getCompletedFlashcardsCount(userId)

            // Підрахунок середнього балу
            val averageScore = getAverageScore(userId)

            // Отримання серії навчання з локальних налаштувань
            val studyDaysStreak = preferencesManager.getLearningStreak()

            // Оновлення серії при заході в додаток
            preferencesManager.updateLoginStreak()

            return UserStatistics(
                userId = userId,
                learnedWordsCount = learnedWordsCount,
                completedChapters = existingStats?.completedChapters ?: 0,
                totalChapters = 8, // Фіксована кількість розділів
                studyDaysStreak = studyDaysStreak,
                totalStudyTimeMinutes = existingStats?.totalStudyTimeMinutes ?: 0,
                favoriteWordsCount = favoriteWordsCount,
                flashcardsCompleted = flashcardsCompleted,
                averageScore = averageScore,
                lastActivityDate = DateUtils.getCurrentDate(),
                registrationDate = existingStats?.registrationDate ?: DateUtils.getCurrentDate()
            )

        } catch (e: Exception) {
            throw e
        }
    }

    private suspend fun getUserVocabularyCount(userId: String): Int {
        return try {
            val documents = userVocabularyCollection
                .whereEqualTo("user_id", userId)
                .get()
                .await()
            documents.size()
        } catch (e: Exception) {
            0
        }
    }

    private suspend fun getFavoriteWordsCount(userId: String): Int {
        return try {
            val documents = userVocabularyCollection
                .whereEqualTo("user_id", userId)
                .whereEqualTo("is_favorite", true)
                .get()
                .await()
            documents.size()
        } catch (e: Exception) {
            0
        }
    }

    private suspend fun getCompletedFlashcardsCount(userId: String): Int {
        return try {
            val documents = userFlashcardProgressCollection
                .whereEqualTo("user_id", userId)
                .whereGreaterThan("success_streak", 2) // Вважаємо вивченим якщо більше 2 правильних відповідей підряд
                .get()
                .await()
            documents.size()
        } catch (e: Exception) {
            0
        }
    }

    private suspend fun getAverageScore(userId: String): Double {
        return try {
            val documents = userFlashcardProgressCollection
                .whereEqualTo("user_id", userId)
                .get()
                .await()

            if (documents.isEmpty) {
                return 0.0
            }

            var totalScore = 0.0
            var totalAttempts = 0

            documents.forEach { document ->
                val successStreak = document.getLong("success_streak")?.toInt() ?: 0
                val attempts = document.getLong("attempts")?.toInt() ?: 1

                if (attempts > 0) {
                    val score = (successStreak.toDouble() / attempts) * 100
                    totalScore += score
                    totalAttempts++
                }
            }

            if (totalAttempts > 0) totalScore / totalAttempts else 0.0

        } catch (e: Exception) {
            0.0
        }
    }

    suspend fun saveUserStatistics(statistics: UserStatistics): Boolean {
        return try {
            statisticsCollection.document(statistics.userId).set(statistics).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun updateCompletedChapters(userId: String, completedChapters: Int): Boolean {
        return try {
            statisticsCollection.document(userId)
                .update("completed_chapters", completedChapters)
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun updateStudyTime(userId: String, additionalMinutes: Int): Boolean {
        return try {
            val currentStats = getUserStatistics()
            val newTotalTime = (currentStats?.totalStudyTimeMinutes ?: 0) + additionalMinutes

            statisticsCollection.document(userId)
                .update("total_study_time_minutes", newTotalTime)
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun incrementLearnedWords(userId: String): Boolean {
        return try {
            val currentStats = getUserStatistics()
            val newCount = (currentStats?.learnedWordsCount ?: 0) + 1

            statisticsCollection.document(userId)
                .update("learned_words_count", newCount)
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }
}