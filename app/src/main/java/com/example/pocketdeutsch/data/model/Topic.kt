package com.example.pocketdeutsch.data.model

import com.google.firebase.firestore.PropertyName

data class Topic(
    @PropertyName("id")
    val id: String = "",

    @PropertyName("chapter_id")
    val chapterId: String = "",

    @PropertyName("title")
    val title: String = "",

    @PropertyName("topic_num")
    val topicNum: Int = 0,

    @PropertyName("description")
    val description: String = ""
) {
    // Порожній конструктор для Firebase
    constructor() : this("", "", "", 0, "")
}