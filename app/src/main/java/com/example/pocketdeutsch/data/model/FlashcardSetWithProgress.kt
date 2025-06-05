package com.example.pocketdeutsch.data.model

import java.util.Date

data class FlashcardSetWithProgress(
    val flashcardSet: FlashcardSet,
    val topic: Topic?,
    val totalCards: Int,
    val studiedCards: Int,
    val averageConfidence: Float,
    val lastStudied: Date?
)