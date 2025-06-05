package com.example.pocketdeutsch.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName

data class UserStatistics(
    @DocumentId
    val id: String = "",

    @get:PropertyName("user_id")
    @set:PropertyName("user_id")
    var userId: String = "",

    @get:PropertyName("learned_words_count")
    @set:PropertyName("learned_words_count")
    var learnedWordsCount: Int = 0,

    @get:PropertyName("flashcards_completed")
    @set:PropertyName("flashcards_completed")
    var flashcardsCompleted: Int = 0,

    @get:PropertyName("average_score")
    @set:PropertyName("average_score")
    var averageScore: Double = 0.0,

    @get:PropertyName("registration_date")
    @set:PropertyName("registration_date")
    var registrationDate: String = "",

    @get:PropertyName("last_activity_date")
    @set:PropertyName("last_activity_date")
    var lastActivityDate: String = "",

    @get:PropertyName("study_days_streak")
    @set:PropertyName("study_days_streak")
    var studyDaysStreak: Int = 0,

    @get:PropertyName("total_study_time_minutes")
    @set:PropertyName("total_study_time_minutes")
    var totalStudyTimeMinutes: Int = 0,

    @get:PropertyName("completed_chapters")
    @set:PropertyName("completed_chapters")
    var completedChapters: Int = 0,

    @get:PropertyName("total_chapters")
    @set:PropertyName("total_chapters")
    var totalChapters: Int = 0,

    @get:PropertyName("favorite_words_count")
    @set:PropertyName("favorite_words_count")
    var favoriteWordsCount: Int = 0
) {
    // Конструктор без параметрів для Firestore
    constructor() : this("", "", 0, 0, 0.0, "", "", 0, 0, 0, 0, 0)
}