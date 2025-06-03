package com.example.pocketdeutsch.data.repository

import android.content.Context
import android.util.Log
import com.example.pocketdeutsch.data.model.*
import com.example.pocketdeutsch.utils.PreferencesManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlin.random.Random

class VocabularyRepository(private val context: Context? = null) {

    private val firestore = FirebaseFirestore.getInstance()
    private val vocabularyCollection = firestore.collection("vocabulary_items")
    private val userVocabularyCollection = firestore.collection("user_vocabulary")
    private val topicsCollection = firestore.collection("topics")
    private val chaptersCollection = firestore.collection("chapters")
    private val textbooksCollection = firestore.collection("textbooks")

    private val preferencesManager = context?.let { PreferencesManager(it) }
    private val auth = FirebaseAuth.getInstance()

    companion object {
        private const val TAG = "VocabularyRepository"
    }

    private fun getCurrentUserId(): String? = auth.currentUser?.uid

    // Додайте цей метод до вашого VocabularyRepository класу

    // Отримання всіх підручників
    suspend fun getAllTextbooks(): List<Textbook> {
        return try {
            Log.d(TAG, "🔍 Завантаження всіх підручників...")

            val documents = textbooksCollection.get().await()
            Log.d(TAG, "📊 Знайдено ${documents.size()} підручників")

            val textbooks = documents.mapNotNull { document ->
                try {
                    val data = document.data
                    Textbook(
                        id = document.id,
                        title = data["title"]?.toString() ?: "",
                        description = data["description"]?.toString() ?: "",
                        author = data["author"]?.toString() ?: "",
                        languageLevel = data["language_level"]?.toString() ?: ""
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Помилка обробки підручника ${document.id}: ${e.message}", e)
                    null
                }
            }

            Log.d(TAG, "✅ Успішно завантажено ${textbooks.size} підручників")
            textbooks
        } catch (e: Exception) {
            Log.e(TAG, "❌ Помилка завантаження підручників: ${e.message}", e)
            throw e
        }
    }

    // Отримання всіх словникових одиниць
    suspend fun getAllWords(): List<VocabularyItem> {
        return try {
            Log.d(TAG, "🔍 Завантаження всіх слів...")

            val documents = vocabularyCollection.get().await()
            Log.d(TAG, "📊 Знайдено ${documents.size()} документів у колекції vocabulary_items")

            if (documents.isEmpty) {
                Log.w(TAG, "⚠️ Колекція vocabulary_items порожня!")
                return emptyList()
            }

            val words = documents.mapNotNull { document ->
                try {
                    Log.d(TAG, "📄 Обробка документа: ${document.id}")
                    Log.d(TAG, "📄 Дані документа: ${document.data}")

                    val data = document.data

                    val word = VocabularyItem(
                        id = document.id,
                        topicId = data["topic_id"]?.toString() ?: "",
                        wordDe = data["word_de"]?.toString() ?: "",
                        translation = data["translation"]?.toString() ?: "",
                        partOfSpeech = data["part_of_speech"]?.toString() ?: "",
                        gender = data["gender"]?.toString() ?: "",
                        pluralForm = data["plural_form"]?.toString() ?: "",
                        exampleSentence = data["example_sentence"]?.toString() ?: "",
                        audioUrl = data["audio_url"]?.toString() ?: ""
                    )

                    Log.d(TAG, "✅ Слово створено: ${word.wordDe} -> ${word.translation}")
                    word
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Помилка обробки документа ${document.id}: ${e.message}", e)
                    null
                }
            }

            Log.d(TAG, "✅ Успішно завантажено ${words.size} слів")
            words.forEach { word ->
                Log.d(TAG, "📝 ${word.wordDe} -> ${word.translation} (ID: ${word.id}, Topic: ${word.topicId})")
            }

            words
        } catch (e: Exception) {
            Log.e(TAG, "❌ Критична помилка завантаження слів: ${e.message}", e)
            throw e
        }
    }

    // Отримання слів по темі
    suspend fun getWordsByTopic(topicId: String): List<VocabularyItem> {
        return try {
            Log.d(TAG, "🔍 Пошук слів по темі: $topicId")

            // Конвертуємо topicId в Int для пошуку
            val topicIdInt = topicId.toIntOrNull()
            Log.d(TAG, "🔍 Topic ID as Int: $topicIdInt")

            val documents = if (topicIdInt != null) {
                vocabularyCollection
                    .whereEqualTo("topic_id", topicIdInt)
                    .get()
                    .await()
            } else {
                vocabularyCollection
                    .whereEqualTo("topic_id", topicId)
                    .get()
                    .await()
            }

            Log.d(TAG, "📊 Знайдено ${documents.size()} документів для теми $topicId")

            val words = documents.mapNotNull { document ->
                try {
                    val data = document.data
                    VocabularyItem(
                        id = document.id,
                        topicId = data["topic_id"]?.toString() ?: "",
                        wordDe = data["word_de"]?.toString() ?: "",
                        translation = data["translation"]?.toString() ?: "",
                        partOfSpeech = data["part_of_speech"]?.toString() ?: "",
                        gender = data["gender"]?.toString() ?: "",
                        pluralForm = data["plural_form"]?.toString() ?: "",
                        exampleSentence = data["example_sentence"]?.toString() ?: "",
                        audioUrl = data["audio_url"]?.toString() ?: ""
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Помилка обробки документа ${document.id}: ${e.message}", e)
                    null
                }
            }

            Log.d(TAG, "✅ Успішно завантажено ${words.size} слів для теми $topicId")
            words
        } catch (e: Exception) {
            Log.e(TAG, "❌ Помилка завантаження слів по темі $topicId: ${e.message}", e)
            throw e
        }
    }

    // Отримання слів по частині мови
    suspend fun getWordsByPartOfSpeech(partOfSpeech: String): List<VocabularyItem> {
        return try {
            Log.d(TAG, "🔍 Пошук слів по частині мови: $partOfSpeech")

            val documents = vocabularyCollection
                .whereEqualTo("part_of_speech", partOfSpeech)
                .get()
                .await()

            Log.d(TAG, "📊 Знайдено ${documents.size()} документів для частини мови $partOfSpeech")

            val words = documents.mapNotNull { document ->
                try {
                    val data = document.data
                    VocabularyItem(
                        id = document.id,
                        topicId = data["topic_id"]?.toString() ?: "",
                        wordDe = data["word_de"]?.toString() ?: "",
                        translation = data["translation"]?.toString() ?: "",
                        partOfSpeech = data["part_of_speech"]?.toString() ?: "",
                        gender = data["gender"]?.toString() ?: "",
                        pluralForm = data["plural_form"]?.toString() ?: "",
                        exampleSentence = data["example_sentence"]?.toString() ?: "",
                        audioUrl = data["audio_url"]?.toString() ?: ""
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Помилка обробки документа ${document.id}: ${e.message}", e)
                    null
                }
            }

            words
        } catch (e: Exception) {
            Log.e(TAG, "❌ Помилка завантаження слів по частині мови: ${e.message}", e)
            throw e
        }
    }

    // Отримання слів по розділу
    suspend fun getWordsByChapter(chapterId: String): List<VocabularyItem> {
        return try {
            Log.d(TAG, "🔍 Пошук слів по розділу: $chapterId")

            // Конвертуємо chapterId в Int
            val chapterIdInt = chapterId.toIntOrNull()

            // Спочатку отримуємо всі теми розділу
            val topicsDocuments = if (chapterIdInt != null) {
                topicsCollection
                    .whereEqualTo("chapter_id", chapterIdInt)
                    .get()
                    .await()
            } else {
                topicsCollection
                    .whereEqualTo("chapter_id", chapterId)
                    .get()
                    .await()
            }

            Log.d(TAG, "📊 Знайдено ${topicsDocuments.size()} тем для розділу $chapterId")

            val topicIds = topicsDocuments.mapNotNull { document ->
                try {
                    document.data["id"]?.toString()
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Помилка отримання ID теми: ${e.message}", e)
                    null
                }
            }

            Log.d(TAG, "📋 Topic IDs: $topicIds")

            if (topicIds.isEmpty()) {
                Log.w(TAG, "⚠️ Не знайдено тем для розділу $chapterId")
                return emptyList()
            }

            // Отримуємо слова для всіх тем цього розділу
            val allWords = mutableListOf<VocabularyItem>()
            for (topicId in topicIds) {
                val words = getWordsByTopic(topicId)
                allWords.addAll(words)
                Log.d(TAG, "➕ Додано ${words.size} слів з теми $topicId")
            }

            Log.d(TAG, "✅ Загалом завантажено ${allWords.size} слів для розділу $chapterId")
            allWords
        } catch (e: Exception) {
            Log.e(TAG, "❌ Помилка завантаження слів по розділу: ${e.message}", e)
            throw e
        }
    }

    // Отримання всіх тем
    suspend fun getAllTopics(): List<Topic> {
        return try {
            Log.d(TAG, "🔍 Завантаження всіх тем...")

            val documents = topicsCollection.get().await()
            Log.d(TAG, "📊 Знайдено ${documents.size()} тем")

            val topics = documents.mapNotNull { document ->
                try {
                    val data = document.data
                    Topic(
                        id = document.id,
                        chapterId = data["chapter_id"]?.toString() ?: "",
                        title = data["title"]?.toString() ?: "",
                        topicNum = (data["topic_num"] as? Long)?.toInt() ?: 0,
                        description = data["description"]?.toString() ?: ""
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Помилка обробки теми ${document.id}: ${e.message}", e)
                    null
                }
            }

            Log.d(TAG, "✅ Успішно завантажено ${topics.size} тем")
            topics
        } catch (e: Exception) {
            Log.e(TAG, "❌ Помилка завантаження тем: ${e.message}", e)
            throw e
        }
    }

    // Отримання тем по розділу
    suspend fun getTopicsByChapter(chapterId: String): List<Topic> {
        return try {
            val chapterIdInt = chapterId.toIntOrNull()

            val documents = if (chapterIdInt != null) {
                topicsCollection
                    .whereEqualTo("chapter_id", chapterIdInt)
                    .get()
                    .await()
            } else {
                topicsCollection
                    .whereEqualTo("chapter_id", chapterId)
                    .get()
                    .await()
            }

            documents.mapNotNull { document ->
                try {
                    val data = document.data
                    Topic(
                        id = document.id,
                        chapterId = data["chapter_id"]?.toString() ?: "",
                        title = data["title"]?.toString() ?: "",
                        topicNum = (data["topic_num"] as? Long)?.toInt() ?: 0,
                        description = data["description"]?.toString() ?: ""
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Помилка обробки теми ${document.id}: ${e.message}", e)
                    null
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Помилка завантаження тем по розділу: ${e.message}", e)
            throw e
        }
    }

    // Отримання всіх розділів
    suspend fun getAllChapters(): List<Chapter> {
        return try {
            val documents = chaptersCollection.get().await()
            documents.mapNotNull { document ->
                try {
                    val data = document.data
                    Chapter(
                        id = document.id,
                        textbookId = data["textbook_id"]?.toString() ?: "",
                        title = data["title"]?.toString() ?: "",
                        chapterNum = (data["chapter_num"] as? Long)?.toInt() ?: 0,
                        description = data["description"]?.toString() ?: ""
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Помилка обробки розділу ${document.id}: ${e.message}", e)
                    null
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Помилка завантаження розділів: ${e.message}", e)
            throw e
        }
    }

    // Випадкове слово дня
    suspend fun getWordOfTheDay(): VocabularyItem? {
        return try {
            preferencesManager?.let { prefs ->
                val (wordId, germanWord, translation) = prefs.getTodaysWordOfDay()
                if (wordId != null && germanWord != null && translation != null) {
                    return VocabularyItem(
                        id = wordId,
                        wordDe = germanWord,
                        translation = translation
                    )
                }
            }

            getRandomWord()
        } catch (e: Exception) {
            Log.e(TAG, "❌ Помилка отримання слова дня: ${e.message}", e)
            throw e
        }
    }

    suspend fun getRandomWord(): VocabularyItem? {
        return try {
            val documents = vocabularyCollection.get().await()

            if (documents.isEmpty) {
                return null
            }

            val randomIndex = Random.nextInt(documents.size())
            val randomDocument = documents.documents[randomIndex]
            val data = randomDocument.data

            val vocabularyItem = VocabularyItem(
                id = randomDocument.id,
                topicId = data?.get("topic_id")?.toString() ?: "",
                wordDe = data?.get("word_de")?.toString() ?: "",
                translation = data?.get("translation")?.toString() ?: "",
                partOfSpeech = data?.get("part_of_speech")?.toString() ?: "",
                gender = data?.get("gender")?.toString() ?: "",
                pluralForm = data?.get("plural_form")?.toString() ?: "",
                exampleSentence = data?.get("example_sentence")?.toString() ?: "",
                audioUrl = data?.get("audio_url")?.toString() ?: ""
            )

            vocabularyItem.let { word ->
                preferencesManager?.let { prefs ->
                    if (prefs.isNewDay()) {
                        prefs.saveWordOfTheDay(word.id, word.wordDe, word.translation)
                    }
                }
            }

            vocabularyItem
        } catch (e: Exception) {
            Log.e(TAG, "❌ Помилка отримання випадкового слова: ${e.message}", e)
            throw e
        }
    }

    // Пошук слів
    suspend fun searchWords(query: String): List<VocabularyItem> {
        return try {
            Log.d(TAG, "🔍 Пошук слів за запитом: '$query'")

            val allWords = getAllWords()
            Log.d(TAG, "📊 Загалом слів для пошуку: ${allWords.size}")

            val filteredWords = allWords.filter { word ->
                word.wordDe.contains(query, ignoreCase = true) ||
                        word.translation.contains(query, ignoreCase = true) ||
                        word.exampleSentence.contains(query, ignoreCase = true)
            }

            Log.d(TAG, "✅ Знайдено ${filteredWords.size} слів за запитом '$query'")
            filteredWords
        } catch (e: Exception) {
            Log.e(TAG, "❌ Помилка пошуку слів: ${e.message}", e)
            throw e
        }
    }

    // Улюблені слова
    suspend fun getFavoriteWords(): List<VocabularyItem> {
        return try {
            val userId = getCurrentUserId()
            Log.d(TAG, "🔍 Завантаження улюблених слів для користувача: $userId")

            if (userId == null) {
                Log.w(TAG, "⚠️ Користувач не авторизований")
                return emptyList()
            }

            val favoriteDocuments = userVocabularyCollection
                .whereEqualTo("user_id", userId)
                .whereEqualTo("favorite", true)
                .get()
                .await()

            Log.d(TAG, "📊 Знайдено ${favoriteDocuments.size()} улюблених записів")

            // ДОДАНО: Логування кожного документа
            favoriteDocuments.forEach { doc ->
                Log.d(TAG, "📄 User vocabulary doc: ${doc.id}")
                Log.d(TAG, "📄 Data: ${doc.data}")
            }

            val favoriteWordIds = favoriteDocuments.mapNotNull { document ->
                try {
                    val data = document.data
                    val wordId = data["vocabulary_item_id"]?.toString()
                    Log.d(TAG, "📋 Extracted word ID: $wordId")
                    wordId
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Помилка отримання ID слова: ${e.message}", e)
                    null
                }
            }

            Log.d(TAG, "📋 Favorite word IDs: $favoriteWordIds")

            if (favoriteWordIds.isEmpty()) {
                Log.d(TAG, "📝 Немає улюблених слів")
                return emptyList()
            }

            val words = mutableListOf<VocabularyItem>()
            for (wordId in favoriteWordIds) {
                Log.d(TAG, "🔍 Завантаження слова з ID: $wordId")
                val word = getWordById(wordId)
                if (word != null) {
                    words.add(word)
                    Log.d(TAG, "✅ Завантажено слово: ${word.wordDe}")
                } else {
                    Log.w(TAG, "⚠️ Слово з ID $wordId не знайдено")
                }
            }

            Log.d(TAG, "✅ Загалом завантажено ${words.size} улюблених слів")
            words
        } catch (e: Exception) {
            Log.e(TAG, "❌ Помилка завантаження улюблених слів: ${e.message}", e)
            throw e
        }
    }

    suspend fun getWordById(wordId: String): VocabularyItem? {
        return try {
            Log.d(TAG, "🔍 Пошук слова за ID: $wordId")

            val document = vocabularyCollection.document(wordId).get().await()

            if (document.exists()) {
                val data = document.data!!
                val word = VocabularyItem(
                    id = document.id,
                    topicId = data["topic_id"]?.toString() ?: "",
                    wordDe = data["word_de"]?.toString() ?: "",
                    translation = data["translation"]?.toString() ?: "",
                    partOfSpeech = data["part_of_speech"]?.toString() ?: "",
                    gender = data["gender"]?.toString() ?: "",
                    pluralForm = data["plural_form"]?.toString() ?: "",
                    exampleSentence = data["example_sentence"]?.toString() ?: "",
                    audioUrl = data["audio_url"]?.toString() ?: ""
                )

                Log.d(TAG, "✅ Знайдено слово: ${word.wordDe}")
                return word
            } else {
                Log.w(TAG, "⚠️ Слово з ID $wordId не знайдено")
            }

            null
        } catch (e: Exception) {
            Log.e(TAG, "❌ Помилка пошуку слова за ID: ${e.message}", e)
            throw e
        }
    }

    suspend fun addToFavorites(vocabularyItemId: String): Boolean {
        return try {
            val userId = getCurrentUserId() ?: return false
            Log.d(TAG, "➕ Додаємо слово $vocabularyItemId до улюблених користувача $userId")

            val existingDocuments = userVocabularyCollection
                .whereEqualTo("user_id", userId)
                .whereEqualTo("vocabulary_item_id", vocabularyItemId)
                .get()
                .await()

            Log.d(TAG, "🔍 Знайдено ${existingDocuments.size()} існуючих записів")

            if (existingDocuments.isEmpty) {
                // Створюємо мапу даних вручну для забезпечення правильних назв полів
                val userVocabularyData = mapOf(
                    "user_id" to userId,
                    "vocabulary_item_id" to vocabularyItemId,
                    "favorite" to true
                )

                Log.d(TAG, "📝 Створюємо новий запис з даними: $userVocabularyData")

                val docRef = userVocabularyCollection.add(userVocabularyData).await()
                Log.d(TAG, "✅ Створено новий запис з ID: ${docRef.id}")

                // Перевіряємо збережений документ
                val savedDoc = userVocabularyCollection.document(docRef.id).get().await()
                Log.d(TAG, "🔍 Перевіряємо збережений документ:")
                Log.d(TAG, "    - Document exists: ${savedDoc.exists()}")
                Log.d(TAG, "    - Document data: ${savedDoc.data}")

            } else {
                val document = existingDocuments.documents.first()
                Log.d(TAG, "📝 Оновлюємо існуючий запис: ${document.id}")
                userVocabularyCollection.document(document.id)
                    .update("favorite", true)
                    .await()
                Log.d(TAG, "✅ Оновлено існуючий запис в улюблених")
            }

            // Тестовий запит відразу після збереження
            Log.d(TAG, "🧪 Тестовий запит відразу після збереження:")
            val testQuery = userVocabularyCollection
                .whereEqualTo("user_id", userId)
                .whereEqualTo("favorite", true)
                .get()
                .await()

            Log.d(TAG, "🧪 Тестовий запит знайшов ${testQuery.size()} записів")
            testQuery.forEach { doc ->
                Log.d(TAG, "🧪 Test doc: ${doc.id}, data: ${doc.data}")
            }

            true
        } catch (e: Exception) {
            Log.e(TAG, "❌ Помилка додавання до улюблених: ${e.message}", e)
            false
        }
    }

    suspend fun removeFromFavorites(vocabularyItemId: String): Boolean {
        return try {
            val userId = getCurrentUserId() ?: return false
            Log.d(TAG, "➖ Видаляємо слово $vocabularyItemId з улюблених користувача $userId")

            val documents = userVocabularyCollection
                .whereEqualTo("user_id", userId)
                .whereEqualTo("vocabulary_item_id", vocabularyItemId)
                .get()
                .await()

            if (documents.size() > 0) {
                val document = documents.documents.first()
                userVocabularyCollection.document(document.id)
                    .update("favorite", false) // Виправлено: використовуємо "favorite"
                    .await()
                Log.d(TAG, "✅ Видалено з улюблених")
                return true
            }

            Log.w(TAG, "⚠️ Запис не знайдено для видалення")
            false
        } catch (e: Exception) {
            Log.e(TAG, "❌ Помилка видалення з улюблених: ${e.message}", e)
            false
        }
    }

    suspend fun isWordFavorite(vocabularyItemId: String): Boolean {
        return try {
            val userId = getCurrentUserId() ?: return false

            val documents = userVocabularyCollection
                .whereEqualTo("user_id", userId)
                .whereEqualTo("vocabulary_item_id", vocabularyItemId)
                .whereEqualTo("favorite", true)
                .get()
                .await()

            documents.size() > 0
        } catch (e: Exception) {
            Log.e(TAG, "❌ Помилка перевірки улюбленого: ${e.message}", e)
            false
        }
    }
}