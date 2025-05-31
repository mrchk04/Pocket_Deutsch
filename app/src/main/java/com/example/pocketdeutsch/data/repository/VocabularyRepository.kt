package com.example.pocketdeutsch.data.repository

import android.content.Context
import com.example.pocketdeutsch.data.model.VocabularyItem
import com.example.pocketdeutsch.utils.PreferencesManager
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlin.random.Random

class VocabularyRepository(private val context: Context? = null) {

    private val firestore = FirebaseFirestore.getInstance()
    private val vocabularyCollection = firestore.collection("vocabulary_item")
    private val preferencesManager = context?.let { PreferencesManager(it) }

    suspend fun getWordOfTheDay(): VocabularyItem? {
        try {
            // Перевіряємо, чи є збережене слово дня для сьогодні
            preferencesManager?.let { prefs ->
                val (wordId, germanWord, translation) = prefs.getTodaysWordOfDay()
                if (wordId != null && germanWord != null && translation != null) {
                    // Повертаємо збережене слово
                    return VocabularyItem(
                        id = wordId,
                        wordDe = germanWord,
                        translation = translation
                    )
                }
            }

            // Якщо немає збереженого слова або новий день, вибираємо нове
            return getRandomWord()

        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun getRandomWord(): VocabularyItem? {
        try {
            // Отримуємо всі слова з колекції
            val documents = vocabularyCollection.get().await()

            if (documents.isEmpty) {
                return null
            }

            // Вибираємо випадкове слово
            val randomIndex = Random.nextInt(documents.size())
            val randomDocument = documents.documents[randomIndex]

            val vocabularyItem = randomDocument.toObject(VocabularyItem::class.java)?.copy(
                id = randomDocument.id
            )

            // Зберігаємо як слово дня, якщо це новий день
            vocabularyItem?.let { word ->
                preferencesManager?.let { prefs ->
                    if (prefs.isNewDay()) {
                        prefs.saveWordOfTheDay(word.id, word.wordDe, word.translation)
                    }
                }
            }

            return vocabularyItem

        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun getWordsByTopic(topicId: String): List<VocabularyItem> {
        try {
            val documents = vocabularyCollection
                .whereEqualTo("topic_id", topicId)
                .get()
                .await()

            return documents.map { document ->
                document.toObject(VocabularyItem::class.java).copy(id = document.id)
            }
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun getWordById(wordId: String): VocabularyItem? {
        try {
            val document = vocabularyCollection.document(wordId).get().await()

            if (document.exists()) {
                return document.toObject(VocabularyItem::class.java)?.copy(id = document.id)
            }

            return null
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun addWord(vocabularyItem: VocabularyItem): String {
        try {
            val documentRef = vocabularyCollection.add(vocabularyItem).await()
            return documentRef.id
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun updateWord(vocabularyItem: VocabularyItem): Boolean {
        try {
            if (vocabularyItem.id.isEmpty()) {
                return false
            }

            vocabularyCollection.document(vocabularyItem.id).set(vocabularyItem).await()
            return true
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun deleteWord(wordId: String): Boolean {
        try {
            vocabularyCollection.document(wordId).delete().await()
            return true
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun searchWords(query: String): List<VocabularyItem> {
        try {
            // Пошук по німецькому слову (базовий пошук)
            val documentsGerman = vocabularyCollection
                .whereGreaterThanOrEqualTo("word_de", query)
                .whereLessThanOrEqualTo("word_de", query + "\uf8ff")
                .get()
                .await()

            val germanResults = documentsGerman.map { document ->
                document.toObject(VocabularyItem::class.java).copy(id = document.id)
            }

            // Пошук по перекладу
            val documentsTranslation = vocabularyCollection
                .whereGreaterThanOrEqualTo("translation", query)
                .whereLessThanOrEqualTo("translation", query + "\uf8ff")
                .get()
                .await()

            val translationResults = documentsTranslation.map { document ->
                document.toObject(VocabularyItem::class.java).copy(id = document.id)
            }

            // Об'єднуємо результати та видаляємо дублікати
            return (germanResults + translationResults).distinctBy { it.id }

        } catch (e: Exception) {
            throw e
        }
    }
}
