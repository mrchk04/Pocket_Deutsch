package com.example.pocketdeutsch.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName

data class Topic(
    @DocumentId
    val id: String = "",

    // Залишаємо String, але додаємо правильний PropertyName mapping
    @get:PropertyName("chapter_id")
    @set:PropertyName("chapter_id")
    var chapterId: String = "",

    @get:PropertyName("title")
    @set:PropertyName("title")
    var title: String = "",

    @get:PropertyName("topic_num")
    @set:PropertyName("topic_num")
    var topicNum: Int = 0,

    @get:PropertyName("description")
    @set:PropertyName("description")
    var description: String = ""
) {
    constructor() : this("", "", "", 0, "")
}