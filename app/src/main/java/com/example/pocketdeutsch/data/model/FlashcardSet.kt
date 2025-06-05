package com.example.pocketdeutsch.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName

data class FlashcardSet(
    @DocumentId
    val id: String = "",

    @get:PropertyName("topic_id")
    @set:PropertyName("topic_id")
    var topicId: Long = 0,

    @get:PropertyName("title")
    @set:PropertyName("title")
    var title: String = "",

    @get:PropertyName("description")
    @set:PropertyName("description")
    var description: String = "",

    @get:PropertyName("word_count")
    @set:PropertyName("word_count")
    var wordCount: Long = 0
) {
    // Конструктор без параметрів для Firestore
    constructor() : this("", 0, "", "", 0)

    // Додаткові методи для зручності (якщо потрібні рядкові ID)
    fun getTopicIdAsString(): String = topicId.toString()
    fun getWordCountAsInt(): Int = wordCount.toInt()
}