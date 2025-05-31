package com.example.pocketdeutsch.data.model

import com.google.firebase.firestore.PropertyName

data class Textbook(
    @PropertyName("id")
    val id: String = "",

    @PropertyName("title")
    val title: String = "",

    @PropertyName("description")
    val description: String = "",

    @PropertyName("author")
    val author: String = "",

    @PropertyName("language_level")
    val languageLevel: String = ""
) {
    // Порожній конструктор для Firebase
    constructor() : this("", "", "", "", "")
}