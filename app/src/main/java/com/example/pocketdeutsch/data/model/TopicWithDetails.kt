package com.example.pocketdeutsch.data.model

// Допоміжна модель для повної інформації про тему
data class TopicWithDetails(
    val topic: Topic,
    val words: List<VocabularyItem>,
    val chapter: Chapter?
)