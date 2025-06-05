package com.example.pocketdeutsch.data.model

data class FlashcardSetStatistics(
    val totalCards: Int = 0,
    val studiedCards: Int = 0,
    val averageConfidence: Float = 0f,
    val progressPercentage: Int = 0
)