package com.example.pocketdeutsch.domain.usecases

import com.example.pocketdeutsch.data.model.Topic
import com.example.pocketdeutsch.data.model.VocabularyItem
import com.example.pocketdeutsch.data.repository.VocabularyRepository
import com.google.firebase.auth.FirebaseAuth

// Базовий клас для результатів
sealed class VocabularyResult<out T> {
    data class Success<T>(val data: T) : VocabularyResult<T>()
    data class Error(val exception: Throwable) : VocabularyResult<Nothing>()
}

// Винятки згідно нашої таблиці рішень
class UserNotAuthenticatedException : Exception("Користувач не автентифікований") // C1=0
class DatabaseUnavailableException : Exception("База даних недоступна") // C2=0
class TopicNotFoundException : Exception("Тему не знайдено") // C4=0
class NoWordsFoundException : Exception("У темі немає слів") // C5=0

// R1, R2, R4, R5: Отримання списку тем (E1)
class GetTopicsUseCase(private val repository: VocabularyRepository) {
    suspend fun execute(): VocabularyResult<List<Topic>> {
        return try {
            // Перевіряємо автентифікацію (C1)
            val user = FirebaseAuth.getInstance().currentUser
                ?: return VocabularyResult.Error(UserNotAuthenticatedException())

            // Спробуємо завантажити теми (C2 - БД доступна)
            val topics = repository.getAllTopics()
            VocabularyResult.Success(topics)
        } catch (e: Exception) {
            VocabularyResult.Error(DatabaseUnavailableException())
        }
    }
}

// R2, R3: Завантаження слів для теми (E3)
class LoadWordsUseCase(private val repository: VocabularyRepository) {
    suspend fun execute(topicId: String): VocabularyResult<List<VocabularyItem>> {
        return try {
            // Перевіряємо базові умови
            val user = FirebaseAuth.getInstance().currentUser
                ?: return VocabularyResult.Error(UserNotAuthenticatedException())

            if (topicId.isEmpty()) {
                return VocabularyResult.Error(TopicNotFoundException())
            }

            // Завантажуємо слова (C4, C5)
            val words = repository.getWordsByTopic(topicId)

            if (words.isEmpty()) {
                return VocabularyResult.Error(NoWordsFoundException())
            }

            VocabularyResult.Success(words)
        } catch (e: Exception) {
            VocabularyResult.Error(e)
        }
    }
}

// R3: Обробка улюблених слів (E9)
class ManageFavoritesUseCase(private val repository: VocabularyRepository) {
    suspend fun addToFavorites(wordId: String): VocabularyResult<Boolean> {
        return try {
            val user = FirebaseAuth.getInstance().currentUser
                ?: return VocabularyResult.Error(UserNotAuthenticatedException())

            val result = repository.addToFavorites(wordId)
            VocabularyResult.Success(result)
        } catch (e: Exception) {
            VocabularyResult.Error(e)
        }
    }

    suspend fun getFavorites(): VocabularyResult<List<VocabularyItem>> {
        return try {
            val user = FirebaseAuth.getInstance().currentUser
                ?: return VocabularyResult.Error(UserNotAuthenticatedException())

            val favorites = repository.getFavoriteWords()
            VocabularyResult.Success(favorites)
        } catch (e: Exception) {
            VocabularyResult.Error(e)
        }
    }
}