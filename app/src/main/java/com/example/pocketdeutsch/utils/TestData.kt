package com.example.pocketdeutsch.utils

import com.example.pocketdeutsch.data.model.FlashcardSet
import com.example.pocketdeutsch.data.model.Topic
import com.example.pocketdeutsch.data.model.User
import com.example.pocketdeutsch.data.model.VocabularyItem

object TestData {
    val mockUser = User(
        id = 1.toString(),
        email = "test@example.com",
        firstName = "Test",
        lastName = "User"
    )

    val mockTopic = Topic(
        id = 1.toString(),
        chapterId = 1.toString(),
        title = "Greetings",
        topicNum = 1,
        description = "Basic greetings"
    )

    val mockVocabularyItems = listOf(
        VocabularyItem(
            id = 1.toString(),
            topicId = 1.toString(),
            wordDe = "Hallo",
            translation = "Привіт",
            partOfSpeech = "interjection",
            exampleSentence = "Hallo, wie geht es dir?"
        ),
        VocabularyItem(
            id = 2.toString(),
            topicId = 1.toString(),
            wordDe = "Danke",
            translation = "Дякую",
            partOfSpeech = "interjection",
            exampleSentence = "Danke schön!"
        )
    )

    val mockFlashcardSet = FlashcardSet(
        id = 1.toString(),
        topicId = 1,
        title = "Test Flashcards",
        wordCount = 2
    )
}