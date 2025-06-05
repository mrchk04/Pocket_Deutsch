package com.example.pocketdeutsch.data.model

data class StudyResults(
    val totalCards: Int = 0,
    val completedCards: Int = 0,
    val correctAnswers: Int = 0,
    val accuracy: Int = 0,
    val studyDuration: Int = 0 // в секундах
)