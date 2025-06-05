package com.example.pocketdeutsch.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName
import java.util.*

data class UserFlashcardProgress(
    @DocumentId
    val id: String = "",

    @get:PropertyName("user_id")
    @set:PropertyName("user_id")
    var userId: String = "",

    @get:PropertyName("flashcard_id")
    @set:PropertyName("flashcard_id")
    var flashcardId: String = "",

    @get:PropertyName("confidence_level")
    @set:PropertyName("confidence_level")
    var confidenceLevel: Int = 0,

    @get:PropertyName("last_practiced")
    @set:PropertyName("last_practiced")
    var lastPracticed: Date? = null,

    @get:PropertyName("success_streak")
    @set:PropertyName("success_streak")
    var successStreak: Int = 0,

    @get:PropertyName("attempts")
    @set:PropertyName("attempts")
    var attempts: Int = 0,

    @get:PropertyName("feedback")
    @set:PropertyName("feedback")
    var feedback: String = ""
) {
    // Конструктор без параметрів для Firestore
    constructor() : this("", "", "", 0, null, 0, 0, "")
}