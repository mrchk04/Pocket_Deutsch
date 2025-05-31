package com.example.pocketdeutsch.data.model

import com.google.firebase.firestore.PropertyName

data class VocabularyItem(
    @PropertyName("id")
    val id: String = "",

    @PropertyName("topic_id")
    val topicId: String = "",

    @PropertyName("german_word")
    val wordDe: String = "",

    @PropertyName("translation")
    val translation: String = "",

    @PropertyName("part_of_speech")
    val partOfSpeech: String = "",

    @PropertyName("gender")
    val gender: String = "",

    @PropertyName("plural_form")
    val pluralForm: String = "",

    @PropertyName("example_sentence")
    val exampleSentence: String = "",

    @PropertyName("audio_url")
    val audioUrl: String = ""
) {
    // Порожній конструктор для Firebase
    constructor() : this("", "", "", "", "", "", "", "", "")
}