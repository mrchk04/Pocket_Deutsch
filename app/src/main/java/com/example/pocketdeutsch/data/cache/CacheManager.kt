package com.example.pocketdeutsch.data.cache

import com.example.pocketdeutsch.data.model.*
import java.util.concurrent.ConcurrentHashMap

object CacheManager {
    private val flashcardSetsCache = ConcurrentHashMap<String, List<FlashcardSet>>()
    private val flashcardsCache = ConcurrentHashMap<String, List<Flashcard>>()
    private val vocabularyCache = ConcurrentHashMap<String, VocabularyItem>()
    private val topicsCache = ConcurrentHashMap<String, Topic>()
    private val userProgressCache = ConcurrentHashMap<String, UserFlashcardProgress>()

    private var lastCacheUpdate = 0L
    private val CACHE_DURATION = 5 * 60 * 1000L // 5 хвилин

    // Flashcard Sets
    fun cacheFlashcardSets(userId: String, sets: List<FlashcardSet>) {
        flashcardSetsCache[userId] = sets
        lastCacheUpdate = System.currentTimeMillis()
    }

    fun getCachedFlashcardSets(userId: String): List<FlashcardSet>? {
        return if (isCacheValid()) flashcardSetsCache[userId] else null
    }

    // Flashcards for specific set
    fun cacheFlashcards(setId: String, flashcards: List<Flashcard>) {
        flashcardsCache[setId] = flashcards
    }

    fun getCachedFlashcards(setId: String): List<Flashcard>? {
        return if (isCacheValid()) flashcardsCache[setId] else null
    }

    // Vocabulary items
    fun cacheVocabularyItem(item: VocabularyItem) {
        vocabularyCache[item.id] = item
    }

    fun getCachedVocabularyItem(id: String): VocabularyItem? {
        return if (isCacheValid()) vocabularyCache[id] else null
    }

    // Topics
    fun cacheTopic(topic: Topic) {
        topicsCache[topic.id] = topic
    }

    fun getCachedTopic(id: String): Topic? {
        return if (isCacheValid()) topicsCache[id] else null
    }

    // User progress
    fun cacheUserProgress(userId: String, flashcardId: String, progress: UserFlashcardProgress) {
        userProgressCache["${userId}_${flashcardId}"] = progress
    }

    fun getCachedUserProgress(userId: String, flashcardId: String): UserFlashcardProgress? {
        return if (isCacheValid()) userProgressCache["${userId}_${flashcardId}"] else null
    }

    // Cache validation
    private fun isCacheValid(): Boolean {
        return System.currentTimeMillis() - lastCacheUpdate < CACHE_DURATION
    }

    // Clear cache when needed
    fun clearCache() {
        flashcardSetsCache.clear()
        flashcardsCache.clear()
        vocabularyCache.clear()
        topicsCache.clear()
        userProgressCache.clear()
        lastCacheUpdate = 0L
    }

    // Clear specific cache
    fun clearFlashcardSetsCache() {
        flashcardSetsCache.clear()
    }

    fun clearFlashcardsCache(setId: String) {
        flashcardsCache.remove(setId)
    }
}