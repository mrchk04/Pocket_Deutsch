package com.example.pocketdeutsch.data.model

data class FlashcardWithVocabulary(
    val flashcard: Flashcard,
    val vocabularyItem: VocabularyItem,
    val progress: UserFlashcardProgress?
)