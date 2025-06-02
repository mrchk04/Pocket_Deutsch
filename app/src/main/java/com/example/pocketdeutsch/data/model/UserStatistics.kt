package com.example.pocketdeutsch.data.model

import com.google.firebase.firestore.PropertyName

data class UserStatistics(
    @PropertyName("user_id")
    val userId: String = "",

    @PropertyName("learned_words_count")
    val learnedWordsCount: Int = 0,

    @PropertyName("completed_chapters")
    val completedChapters: Int = 0,

    @PropertyName("total_chapters")
    val totalChapters: Int = 8, // Загальна кількість розділів у підручнику

    @PropertyName("study_days_streak")
    val studyDaysStreak: Int = 0,

    @PropertyName("total_study_time_minutes")
    val totalStudyTimeMinutes: Int = 0,

    @PropertyName("favorite_words_count")
    val favoriteWordsCount: Int = 0,

    @PropertyName("flashcards_completed")
    val flashcardsCompleted: Int = 0,

    @PropertyName("average_score")
    val averageScore: Double = 0.0,

    @PropertyName("last_activity_date")
    val lastActivityDate: String = "",

    @PropertyName("registration_date")
    val registrationDate: String = ""
) {
    // Порожній конструктор для Firebase
    constructor() : this("", 0, 0, 8, 0, 0, 0, 0, 0.0, "", "")
}