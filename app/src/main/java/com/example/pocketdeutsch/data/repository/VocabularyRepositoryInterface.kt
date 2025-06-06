package com.example.pocketdeutsch.data.repository

import com.example.pocketdeutsch.data.model.*

interface VocabularyRepositoryInterface {
    suspend fun getAllTopics(): List<Topic>
    suspend fun getWordsByTopic(topicId: String): List<VocabularyItem>
    suspend fun addToFavorites(vocabularyItemId: String): Boolean
    suspend fun getFavoriteWords(): List<VocabularyItem>
    suspend fun searchWords(query: String): List<VocabularyItem>
}