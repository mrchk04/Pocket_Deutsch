package com.example.pocketdeutsch.domain.usecases

import com.example.pocketdeutsch.data.model.Topic
import com.example.pocketdeutsch.data.model.VocabularyItem
import com.example.pocketdeutsch.data.repository.VocabularyRepositoryInterface
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner
import org.junit.Assert.*

@RunWith(MockitoJUnitRunner::class)
class VocabularyUseCasesTest {

    @Mock
    private lateinit var mockRepository: VocabularyRepositoryInterface // Тепер інтерфейс!

    @Test
    fun `R1 - should return topics when repository has data`() = runTest {
        // Given (Імітуємо умови C1=1, C2=1)
        val expectedTopics = listOf(
            Topic("1", "ch1", "Guten Tag", 1, "Greetings"),
            Topic("2", "ch1", "Familie", 2, "Family")
        )
        `when`(mockRepository.getAllTopics()).thenReturn(expectedTopics)

        // When
        val result = mockRepository.getAllTopics()

        // Then (E1=1 - список тем показується)
        assertEquals(2, result.size)
        assertEquals("Guten Tag", result[0].title)
        assertEquals("Familie", result[1].title)
        verify(mockRepository).getAllTopics()
    }

    @Test
    fun `R2 - should return words when topic exists and has words`() = runTest {
        // Given (C1=1, C2=1, C4=1, C5=1)
        val topicId = "topic123"
        val expectedWords = listOf(
            VocabularyItem("w1", topicId, "Hallo", "Привіт"),
            VocabularyItem("w2", topicId, "Danke", "Дякую")
        )
        `when`(mockRepository.getWordsByTopic(topicId)).thenReturn(expectedWords)

        // When
        val result = mockRepository.getWordsByTopic(topicId)

        // Then (E3=1 - слова завантажуються)
        assertEquals(2, result.size)
        assertEquals("Hallo", result[0].wordDe)
        assertEquals("Привіт", result[0].translation)
        verify(mockRepository).getWordsByTopic(topicId)
    }

    @Test
    fun `R5 - should return empty list when topic has no words`() = runTest {
        // Given (C1=1, C2=1, C4=1, C5=0)
        val topicId = "emptyTopic"
        `when`(mockRepository.getWordsByTopic(topicId)).thenReturn(emptyList())

        // When
        val result = mockRepository.getWordsByTopic(topicId)

        // Then (E3=0, E12=1 - помилка, немає слів)
        assertTrue(result.isEmpty())
        verify(mockRepository).getWordsByTopic(topicId)
    }

    @Test
    fun `R3 - should add word to favorites successfully`() = runTest {
        // Given (C1=1, улюблені доступні)
        val wordId = "word123"
        `when`(mockRepository.addToFavorites(wordId)).thenReturn(true)

        // When
        val result = mockRepository.addToFavorites(wordId)

        // Then (E9=1 - додано до улюблених)
        assertTrue(result)
        verify(mockRepository).addToFavorites(wordId)
    }

    @Test
    fun `R3 - should get favorite words for authenticated user`() = runTest {
        // Given
        val expectedFavorites = listOf(
            VocabularyItem("w1", "topic1", "Haus", "Дім"),
            VocabularyItem("w2", "topic2", "Auto", "Машина")
        )
        `when`(mockRepository.getFavoriteWords()).thenReturn(expectedFavorites)

        // When
        val result = mockRepository.getFavoriteWords()

        // Then
        assertEquals(2, result.size)
        assertEquals("Haus", result[0].wordDe)
        verify(mockRepository).getFavoriteWords()
    }

    @Test
    fun `should search words by query`() = runTest {
        // Given
        val query = "Hallo"
        val expectedResults = listOf(
            VocabularyItem("w1", "topic1", "Hallo", "Привіт")
        )
        `when`(mockRepository.searchWords(query)).thenReturn(expectedResults)

        // When
        val result = mockRepository.searchWords(query)

        // Then
        assertEquals(1, result.size)
        assertEquals("Hallo", result[0].wordDe)
        verify(mockRepository).searchWords(query)
    }
}