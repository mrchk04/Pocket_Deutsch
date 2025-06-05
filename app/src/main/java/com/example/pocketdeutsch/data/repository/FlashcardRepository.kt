package com.example.pocketdeutsch.data.repository

import android.util.Log
import com.example.pocketdeutsch.data.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await
import java.util.*

class FlashcardRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    suspend fun getUserStatistics(): UserStatistics {
        val userId = auth.currentUser?.uid ?: return UserStatistics()

        try {
            val existingStats = firestore.collection("user_statistics")
                .document(userId)
                .get()
                .await()
                .toObject(UserStatistics::class.java)

            if (existingStats != null) {
                return existingStats
            }

            val newStats = calculateUserStatistics(userId)
            firestore.collection("user_statistics")
                .document(userId)
                .set(newStats)
                .await()

            return newStats
        } catch (e: Exception) {
            Log.e("FlashcardRepository", "Error getting user statistics", e)
            return UserStatistics()
        }
    }

    private suspend fun calculateUserStatistics(userId: String): UserStatistics {
        var learnedWordsCount = 0
        var flashcardsCompleted = 0
        var totalConfidence = 0.0
        var confidenceCount = 0

        try {
            val progressList = firestore.collection("user_flashcard_progress")
                .whereEqualTo("user_id", userId)
                .get()
                .await()
                .toObjects(UserFlashcardProgress::class.java)

            for (progress in progressList) {
                if (progress.attempts > 0) {
                    flashcardsCompleted++

                    if (progress.confidenceLevel >= 4) {
                        learnedWordsCount++
                    }

                    totalConfidence += progress.confidenceLevel
                    confidenceCount++
                }
            }
        } catch (e: Exception) {
            Log.e("FlashcardRepository", "Error calculating statistics", e)
        }

        val averageScore = if (confidenceCount > 0) totalConfidence / confidenceCount else 0.0

        return UserStatistics(
            userId = userId,
            learnedWordsCount = learnedWordsCount,
            flashcardsCompleted = flashcardsCompleted,
            averageScore = averageScore,
            registrationDate = Date().toString()
        )
    }

    suspend fun updateUserStatistics() {
        val userId = auth.currentUser?.uid ?: return

        try {
            val stats = calculateUserStatistics(userId)
            firestore.collection("user_statistics")
                .document(userId)
                .set(stats)
                .await()
        } catch (e: Exception) {
            Log.e("FlashcardRepository", "Error updating statistics", e)
        }
    }

    private suspend fun findVocabularyItem(vocabularyItemId: Long): VocabularyItem? {
        return try {
            // Спочатку пробуємо знайти за document ID як рядок
            var vocabDoc = firestore.collection("vocabulary_items")
                .document(vocabularyItemId.toString())
                .get()
                .await()

            if (vocabDoc.exists()) {
                Log.d("FlashcardRepository", "Found vocabulary item by document ID: $vocabularyItemId")
                return createVocabularyItemFromData(vocabDoc.id, vocabDoc.data)
            }

            // Якщо не знайшли, пробуємо пошук за полем id
            val vocabQuery = firestore.collection("vocabulary_items")
                .whereEqualTo("id", vocabularyItemId)
                .limit(1)
                .get()
                .await()

            if (!vocabQuery.isEmpty) {
                Log.d("FlashcardRepository", "Found vocabulary item by id field: $vocabularyItemId")
                val doc = vocabQuery.documents[0]
                return createVocabularyItemFromData(doc.id, doc.data)
            }

            Log.w("FlashcardRepository", "VocabularyItem not found for id: $vocabularyItemId")
            null

        } catch (e: Exception) {
            Log.e("FlashcardRepository", "Error fetching vocabulary item $vocabularyItemId", e)
            null
        }
    }

    private fun createVocabularyItemFromData(documentId: String, data: Map<String, Any>?): VocabularyItem? {
        if (data == null) return null

        return try {
            // Ручно створюємо VocabularyItem, конвертуючи всі типи
            VocabularyItem(
                id = documentId,
                topicId = (data["topic_id"] as? Long)?.toString() ?: "",
                wordDe = data["word_de"]?.toString() ?: "",
                translation = data["translation"]?.toString() ?: "",
                partOfSpeech = data["part_of_speech"]?.toString() ?: "",
                gender = data["gender"]?.toString() ?: "",
                pluralForm = data["plural_form"]?.toString() ?: "",
                exampleSentence = data["example_sentence"]?.toString() ?: "",
                audioUrl = data["audio_url"]?.toString() ?: ""
            )
        } catch (e: Exception) {
            Log.e("FlashcardRepository", "Error creating VocabularyItem from data", e)
            null
        }
    }

    private suspend fun getTopicSafely(topicId: Long): Topic? {
        return try {
            // Спочатку пробуємо знайти документ
            val topicDoc = firestore.collection("topics")
                .document(topicId.toString())
                .get()
                .await()

            if (topicDoc.exists()) {
                val data = topicDoc.data ?: return null

                // Ручно створюємо Topic, конвертуючи типи
                return Topic(
                    id = topicDoc.id,
                    chapterId = (data["chapter_id"] as? Long)?.toString() ?: "",
                    title = data["title"]?.toString() ?: "",
                    topicNum = (data["topic_num"] as? Long)?.toInt() ?: 0,
                    description = data["description"]?.toString() ?: ""
                )
            }

            // Якщо не знайшли за document ID, пробуємо пошук за полем
            val topicQuery = firestore.collection("topics")
                .whereEqualTo("id", topicId)
                .limit(1)
                .get()
                .await()

            if (!topicQuery.isEmpty) {
                val doc = topicQuery.documents[0]
                val data = doc.data ?: return null

                return Topic(
                    id = doc.id,
                    chapterId = (data["chapter_id"] as? Long)?.toString() ?: "",
                    title = data["title"]?.toString() ?: "",
                    topicNum = (data["topic_num"] as? Long)?.toInt() ?: 0,
                    description = data["description"]?.toString() ?: ""
                )
            }

            null
        } catch (e: Exception) {
            Log.e("FlashcardRepository", "Error loading topic $topicId safely", e)
            null
        }
    }

    suspend fun getFlashcardSetsWithProgress(): List<FlashcardSetWithProgress> {
        val userId = auth.currentUser?.uid ?: return emptyList()
        debugFirestoreStructure()

        try {
            Log.d("FlashcardRepository", "Starting getFlashcardSetsWithProgress for user: $userId")

            // Отримуємо всі набори флеш-карток
            val flashcardSets = firestore.collection("flashcard_sets")
                .get()
                .await()
                .toObjects(FlashcardSet::class.java)

            Log.d("FlashcardRepository", "Found ${flashcardSets.size} flashcard sets")

            return coroutineScope {
                val deferredResults = flashcardSets.map { flashcardSet ->
                    async {
                        Log.d("FlashcardRepository", "Processing flashcard set: ${flashcardSet.title}, ID: ${flashcardSet.id}")

                        // Завантажуємо тему, якщо є topicId
                        val topic = if (flashcardSet.topicId > 0) {
                            Log.d("FlashcardRepository", "Loading topic with ID: ${flashcardSet.topicId}")
                            getTopicSafely(flashcardSet.topicId)
                        } else null

                        // Отримуємо всі флеш-картки в наборі (по числовому flashcard_set_id)
                        val flashcards = try {
                            // Конвертуємо document ID в Long для пошуку
                            val setIdLong = flashcardSet.id.toLongOrNull()

                            if (setIdLong != null) {
                                Log.d("FlashcardRepository", "Searching flashcards for set ID: $setIdLong")
                                firestore.collection("flashcards")
                                    .whereEqualTo("flashcard_set_id", setIdLong) // Шукаємо по числовому значенню
                                    .get()
                                    .await()
                                    .toObjects(Flashcard::class.java)
                            } else {
                                Log.w("FlashcardRepository", "Could not convert set ID to Long: ${flashcardSet.id}")
                                emptyList()
                            }
                        } catch (e: Exception) {
                            Log.e("FlashcardRepository", "Error getting flashcards for set ${flashcardSet.id}", e)
                            emptyList()
                        }

                        Log.d("FlashcardRepository", "Found ${flashcards.size} flashcards in set ${flashcardSet.title}")

                        val totalCards = flashcards.size

                        // Якщо немає флешкарток — повертати результат без прогресу
                        if (flashcards.isEmpty()) {
                            return@async FlashcardSetWithProgress(
                                flashcardSet = flashcardSet,
                                topic = topic,
                                totalCards = 0,
                                studiedCards = 0,
                                averageConfidence = 0f,
                                lastStudied = null
                            )
                        }

                        // Отримаємо прогрес користувача пачками по 10 id
                        var studiedCards = 0
                        var totalConfidence = 0f
                        var confidenceCount = 0
                        var lastStudied: Date? = null

                        val flashcardIds = flashcards.map { it.id }
                        val batches = flashcardIds.chunked(10) // Firestore whereIn max 10

                        for (batch in batches) {
                            val userProgressList = try {
                                firestore.collection("user_flashcard_progress")
                                    .whereEqualTo("user_id", userId)
                                    .whereIn("flashcard_id", batch)
                                    .get()
                                    .await()
                                    .toObjects(UserFlashcardProgress::class.java)
                            } catch (e: Exception) {
                                Log.e("FlashcardRepository", "Error getting user progress for batch", e)
                                emptyList()
                            }

                            for (progress in userProgressList) {
                                if (progress.attempts > 0) {
                                    studiedCards++
                                    totalConfidence += progress.confidenceLevel
                                    confidenceCount++
                                    if (progress.lastPracticed != null) {
                                        if (lastStudied == null || progress.lastPracticed!! > lastStudied!!) {
                                            lastStudied = progress.lastPracticed
                                        }
                                    }
                                }
                            }
                        }

                        val averageConfidence = if (confidenceCount > 0) totalConfidence / confidenceCount else 0f

                        Log.d("FlashcardRepository", "Set ${flashcardSet.title}: $studiedCards/$totalCards studied, avg confidence: $averageConfidence")

                        FlashcardSetWithProgress(
                            flashcardSet = flashcardSet,
                            topic = topic,
                            totalCards = totalCards,
                            studiedCards = studiedCards,
                            averageConfidence = averageConfidence,
                            lastStudied = lastStudied
                        )
                    }
                }

                val result = deferredResults.awaitAll()
                Log.d("FlashcardRepository", "Returning ${result.size} flashcard sets with progress")

                result.sortedBy { it.flashcardSet.title }
            }
        } catch (e: Exception) {
            Log.e("FlashcardRepository", "Error in getFlashcardSetsWithProgress", e)
            throw e
        }
    }

    suspend fun getFlashcardsWithVocabulary(setId: String): List<FlashcardWithVocabulary> {
        val userId = auth.currentUser?.uid ?: return emptyList()

        try {
            // Конвертуємо setId в Long для пошуку
            val setIdLong = setId.toLongOrNull() ?: return emptyList()

            Log.d("FlashcardRepository", "Searching for flashcards with flashcard_set_id: $setIdLong")

            val flashcards = firestore.collection("flashcards")
                .whereEqualTo("flashcard_set_id", setIdLong) // Шукаємо по числовому значенню
                .orderBy("position")
                .get()
                .await()
                .toObjects(Flashcard::class.java)

            Log.d("FlashcardRepository", "Found ${flashcards.size} flashcards for set $setId")

            val result = mutableListOf<FlashcardWithVocabulary>()

            for (flashcard in flashcards) {
                try {
                    if (flashcard.vocabularyItemId > 0) {
                        // Шукаємо vocabulary_item по числовому ID
                        val vocabularyItem = findVocabularyItem(flashcard.vocabularyItemId) ?: continue

                        if (vocabularyItem != null) {
                            val progress = firestore.collection("user_flashcard_progress")
                                .whereEqualTo("user_id", userId)
                                .whereEqualTo("flashcard_id", flashcard.id)
                                .get()
                                .await()
                                .toObjects(UserFlashcardProgress::class.java)
                                .firstOrNull()

                            result.add(
                                FlashcardWithVocabulary(
                                    flashcard = flashcard,
                                    vocabularyItem = vocabularyItem,
                                    progress = progress
                                )
                            )
                        } else {
                            Log.w("FlashcardRepository", "VocabularyItem not found for id: ${flashcard.vocabularyItemId}")
                        }
                    } else {
                        Log.w("FlashcardRepository", "Invalid vocabularyItemId for flashcard: ${flashcard.id}")
                    }
                } catch (e: Exception) {
                    Log.e("FlashcardRepository", "Error processing flashcard ${flashcard.id}", e)
                }
            }

            return result
        } catch (e: Exception) {
            Log.e("FlashcardRepository", "Error getting flashcards with vocabulary", e)
            throw e
        }
    }

    suspend fun debugFirestoreStructure() {
        try {
            Log.d("FlashcardRepository", "=== DEBUGGING FIRESTORE STRUCTURE ===")

            // Перевіряємо структуру flashcard_sets
            val flashcardSetsSnapshot = firestore.collection("flashcard_sets")
                .limit(1)
                .get()
                .await()

            if (!flashcardSetsSnapshot.isEmpty) {
                val firstSet = flashcardSetsSnapshot.documents[0]
                Log.d("FlashcardRepository", "FlashcardSet document ID: ${firstSet.id}")
                Log.d("FlashcardRepository", "FlashcardSet fields: ${firstSet.data}")

                // Перевіряємо конкретні поля
                val topicId = firstSet.get("topic_id")
                val wordCount = firstSet.get("word_count")
                val title = firstSet.get("title")

                Log.d("FlashcardRepository", "topic_id: $topicId (type: ${topicId?.javaClass?.simpleName})")
                Log.d("FlashcardRepository", "word_count: $wordCount (type: ${wordCount?.javaClass?.simpleName})")
                Log.d("FlashcardRepository", "title: $title (type: ${title?.javaClass?.simpleName})")
            }

            // Перевіряємо структуру flashcards
            val flashcardsSnapshot = firestore.collection("flashcards")
                .limit(1)
                .get()
                .await()

            if (!flashcardsSnapshot.isEmpty) {
                val firstFlashcard = flashcardsSnapshot.documents[0]
                Log.d("FlashcardRepository", "Flashcard document ID: ${firstFlashcard.id}")
                Log.d("FlashcardRepository", "Flashcard fields: ${firstFlashcard.data}")

                val flashcardSetId = firstFlashcard.get("flashcard_set_id")
                val vocabularyItemId = firstFlashcard.get("vocabulary_item_id")

                Log.d("FlashcardRepository", "flashcard_set_id: $flashcardSetId (type: ${flashcardSetId?.javaClass?.simpleName})")
                Log.d("FlashcardRepository", "vocabulary_item_id: $vocabularyItemId (type: ${vocabularyItemId?.javaClass?.simpleName})")
            }

            // Перевіряємо кількість документів в кожній колекції
            val setsCount = firestore.collection("flashcard_sets").get().await().size()
            val flashcardsCount = firestore.collection("flashcards").get().await().size()
            val vocabularyCount = firestore.collection("vocabulary_items").get().await().size()

            Log.d("FlashcardRepository", "Collections count:")
            Log.d("FlashcardRepository", "flashcard_sets: $setsCount")
            Log.d("FlashcardRepository", "flashcards: $flashcardsCount")
            Log.d("FlashcardRepository", "vocabulary_items: $vocabularyCount")

            Log.d("FlashcardRepository", "=== VOCABULARY ITEM DIAGNOSIS ===")

// Перевіряємо структуру vocabulary_items
            val vocabularySnapshot = firestore.collection("vocabulary_items")
                .limit(1)
                .get()
                .await()

            if (!vocabularySnapshot.isEmpty) {
                val doc = vocabularySnapshot.documents[0]
                Log.d("FlashcardRepository", "VocabularyItem document ID: ${doc.id}")
                Log.d("FlashcardRepository", "VocabularyItem fields: ${doc.data}")

                val data = doc.data
                if (data != null) {
                    for ((key, value) in data) {
                        Log.d("FlashcardRepository", "Field: $key = $value (type: ${value?.javaClass?.simpleName})")
                    }
                }
            }

// Також покажіть структуру вашої VocabularyItem моделі в логах:
            Log.d("FlashcardRepository", "=== CURRENT VOCABULARY ITEM MODEL ===")
            Log.d("FlashcardRepository", "Please check your VocabularyItem model and tell me:")
            Log.d("FlashcardRepository", "1. What type is 'id' field? (String/Int/Long)")
            Log.d("FlashcardRepository", "2. What type is 'topicId' field? (String/Int/Long)")
            Log.d("FlashcardRepository", "3. Are there any @PropertyName annotations?")

        } catch (e: Exception) {
            Log.e("FlashcardRepository", "Error debugging Firestore structure", e)
        }
    }

    suspend fun getFlashcardSetById(setId: String): FlashcardSet? {
        return try {
            firestore.collection("flashcard_sets")
                .document(setId)
                .get()
                .await()
                .toObject(FlashcardSet::class.java)
        } catch (e: Exception) {
            Log.e("FlashcardRepository", "Error getting flashcard set by id", e)
            null
        }
    }

    suspend fun getSetStatistics(setId: String): FlashcardSetStatistics {
        val userId = auth.currentUser?.uid ?: return FlashcardSetStatistics()

        try {
            val setIdLong = setId.toLongOrNull() ?: return FlashcardSetStatistics()

            val flashcards = firestore.collection("flashcards")
                .whereEqualTo("flashcard_set_id", setIdLong) // Шукаємо по числовому значенню
                .get()
                .await()
                .toObjects(Flashcard::class.java)

            val totalCards = flashcards.size
            var studiedCards = 0
            var totalConfidence = 0f
            var confidenceCount = 0

            for (flashcard in flashcards) {
                val progress = firestore.collection("user_flashcard_progress")
                    .whereEqualTo("user_id", userId)
                    .whereEqualTo("flashcard_id", flashcard.id)
                    .get()
                    .await()
                    .toObjects(UserFlashcardProgress::class.java)
                    .firstOrNull()

                if (progress != null && progress.attempts > 0) {
                    studiedCards++
                    totalConfidence += progress.confidenceLevel
                    confidenceCount++
                }
            }

            val averageConfidence = if (confidenceCount > 0) totalConfidence / confidenceCount else 0f
            val progressPercentage = if (totalCards > 0) (studiedCards * 100) / totalCards else 0

            return FlashcardSetStatistics(
                totalCards = totalCards,
                studiedCards = studiedCards,
                averageConfidence = averageConfidence,
                progressPercentage = progressPercentage
            )
        } catch (e: Exception) {
            Log.e("FlashcardRepository", "Error getting set statistics", e)
            return FlashcardSetStatistics()
        }
    }

    suspend fun getTopic(topicId: String): Topic? {
        val topicIdLong = topicId.toLongOrNull() ?: return null
        return getTopicSafely(topicIdLong)
    }

    // ===== Методи для FlashcardStudyActivity =====

    suspend fun getStudyFlashcards(setId: String, mode: String): List<FlashcardWithVocabulary> {
        val userId = auth.currentUser?.uid ?: return emptyList()

        try {
            // Конвертуємо setId в Long
            val setIdLong = setId.toLongOrNull() ?: return emptyList()

            Log.d("FlashcardRepository", "Getting study flashcards for set $setIdLong, mode: $mode")

            val flashcards = firestore.collection("flashcards")
                .whereEqualTo("flashcard_set_id", setIdLong) // Шукаємо по числовому значенню
                .orderBy("position")
                .get()
                .await()
                .toObjects(Flashcard::class.java)

            Log.d("FlashcardRepository", "Found ${flashcards.size} flashcards for study")

            val result = mutableListOf<FlashcardWithVocabulary>()

            for (flashcard in flashcards) {
                try {
                    if (flashcard.vocabularyItemId > 0) {
                        // Отримуємо словникову одиницю
                        val vocabularyItem = findVocabularyItem(flashcard.vocabularyItemId) ?: continue

                        // Отримуємо прогрес користувача
                        val progress = firestore.collection("user_flashcard_progress")
                            .whereEqualTo("user_id", userId)
                            .whereEqualTo("flashcard_id", flashcard.id)
                            .get()
                            .await()
                            .toObjects(UserFlashcardProgress::class.java)
                            .firstOrNull()

                        val flashcardWithVocabulary = FlashcardWithVocabulary(
                            flashcard = flashcard,
                            vocabularyItem = vocabularyItem,
                            progress = progress
                        )

                        // Фільтруємо залежно від режиму вивчення
                        when (mode) {
                            "new" -> {
                                if (progress == null || progress.attempts == 0) {
                                    result.add(flashcardWithVocabulary)
                                }
                            }
                            "review" -> {
                                if (progress != null && progress.attempts > 0 && progress.confidenceLevel < 4) {
                                    result.add(flashcardWithVocabulary)
                                }
                            }
                            "all" -> {
                                result.add(flashcardWithVocabulary)
                            }
                        }
                    } else {
                        Log.w("FlashcardRepository", "Invalid vocabularyItemId for flashcard: ${flashcard.id}")
                    }
                } catch (e: Exception) {
                    Log.e("FlashcardRepository", "Error processing flashcard ${flashcard.id}", e)
                }
            }

            Log.d("FlashcardRepository", "Returning ${result.size} flashcards for mode: $mode")
            return result
        } catch (e: Exception) {
            Log.e("FlashcardRepository", "Error getting study flashcards", e)
            throw e
        }
    }

    suspend fun updateFlashcardProgress(
        flashcardId: String,
        confidenceLevel: Int,
        isCorrect: Boolean
    ): Boolean {
        val userId = auth.currentUser?.uid ?: return false

        try {
            // Спробуємо знайти існуючий прогрес
            val existingProgress = firestore.collection("user_flashcard_progress")
                .whereEqualTo("user_id", userId)
                .whereEqualTo("flashcard_id", flashcardId)
                .get()
                .await()
                .toObjects(UserFlashcardProgress::class.java)
                .firstOrNull()

            val currentDate = Date()

            if (existingProgress != null) {
                // Оновлюємо існуючий прогрес
                val newSuccessStreak = if (isCorrect) {
                    existingProgress.successStreak + 1
                } else {
                    0
                }

                val updatedProgress = existingProgress.copy(
                    confidenceLevel = confidenceLevel,
                    lastPracticed = currentDate,
                    successStreak = newSuccessStreak,
                    attempts = existingProgress.attempts + 1
                )

                firestore.collection("user_flashcard_progress")
                    .document(existingProgress.id)
                    .set(updatedProgress)
                    .await()
            } else {
                // Створюємо новий прогрес
                val newProgressId = firestore.collection("user_flashcard_progress").document().id
                val newProgress = UserFlashcardProgress(
                    id = newProgressId,
                    userId = userId,
                    flashcardId = flashcardId,
                    confidenceLevel = confidenceLevel,
                    lastPracticed = currentDate,
                    successStreak = if (isCorrect) 1 else 0,
                    attempts = 1
                )

                firestore.collection("user_flashcard_progress")
                    .document(newProgressId)
                    .set(newProgress)
                    .await()
            }

            return true
        } catch (e: Exception) {
            Log.e("FlashcardRepository", "Error updating flashcard progress", e)
            return false
        }
    }

    suspend fun updateUserStatisticsAfterStudy(completedCards: Int) {
        val userId = auth.currentUser?.uid ?: return

        try {
            val statsRef = firestore.collection("user_statistics").document(userId)
            val existingStats = statsRef.get().await().toObject(UserStatistics::class.java)

            if (existingStats != null) {
                val updatedStats = existingStats.copy(
                    flashcardsCompleted = existingStats.flashcardsCompleted + completedCards,
                    lastActivityDate = Date().toString()
                )
                statsRef.set(updatedStats).await()
            } else {
                // Створюємо нову статистику якщо її немає
                val newStats = UserStatistics(
                    userId = userId,
                    flashcardsCompleted = completedCards,
                    lastActivityDate = Date().toString(),
                    registrationDate = Date().toString()
                )
                statsRef.set(newStats).await()
            }
        } catch (e: Exception) {
            Log.e("FlashcardRepository", "Error updating user statistics", e)
        }
    }
}