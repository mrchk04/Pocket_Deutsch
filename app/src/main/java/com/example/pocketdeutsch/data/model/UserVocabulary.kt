package com.example.pocketdeutsch.data.model

import com.google.firebase.firestore.PropertyName

data class UserVocabulary(
    @PropertyName("id")
    val id: String = "",

    @PropertyName("user_id")
    val userId: String = "",

    @PropertyName("vocabulary_item_id")
    val vocabularyItemId: String = "",

    @PropertyName("favorite")
    val favorite: Boolean = false
) {
    // Порожній конструктор для Firebase
    constructor() : this("", "", "", false)
}