package com.example.pocketdeutsch.data.model

import com.google.firebase.firestore.PropertyName

data class Chapter(
    @PropertyName("id")
    val id: String = "",

    @PropertyName("textbook_id")
    val textbookId: String = "",

    @PropertyName("title")
    val title: String = "",

    @PropertyName("chapter_num")
    val chapterNum: Int = 0,

    @PropertyName("description")
    val description: String = ""
) {
    // Порожній конструктор для Firebase
    constructor() : this("", "", "", 0, "")
}