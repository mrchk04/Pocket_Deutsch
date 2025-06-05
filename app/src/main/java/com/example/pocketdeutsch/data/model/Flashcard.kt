package com.example.pocketdeutsch.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName

data class Flashcard(
    @DocumentId
    val id: String = "",

    @get:PropertyName("flashcard_set_id")
    @set:PropertyName("flashcard_set_id")
    var flashcardSetId: Long = 0,

    @get:PropertyName("vocabulary_item_id")
    @set:PropertyName("vocabulary_item_id")
    var vocabularyItemId: Long = 0,

    @get:PropertyName("position")
    @set:PropertyName("position")
    var position: Int = 0
) {
    // Конструктор без параметрів для Firestore
    constructor() : this("", 0, 0, 0)

    // Додаткові методи для зручності
    fun getFlashcardSetIdAsString(): String = flashcardSetId.toString()
    fun getVocabularyItemIdAsString(): String = vocabularyItemId.toString()
}
