package com.example.pocketdeutsch.ui.wordDetails

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.pocketdeutsch.data.model.Chapter
import com.example.pocketdeutsch.data.model.Topic
import com.example.pocketdeutsch.data.model.VocabularyItem
import com.example.pocketdeutsch.data.repository.VocabularyRepository
import kotlinx.coroutines.launch

class WordDetailsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = VocabularyRepository(application)

    private val _isFavorite = MutableLiveData<Boolean>()
    val isFavorite: LiveData<Boolean> = _isFavorite

    private val _topicInfo = MutableLiveData<Topic?>()
    val topicInfo: LiveData<Topic?> = _topicInfo

    private val _chapterInfo = MutableLiveData<Chapter?>()
    val chapterInfo: LiveData<Chapter?> = _chapterInfo

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private var currentWord: VocabularyItem? = null

    private val _wordData = MutableLiveData<VocabularyItem?>()
    val wordData: LiveData<VocabularyItem?> = _wordData

    fun loadWordById(wordId: String) {
        viewModelScope.launch {
            try {
                val word = repository.getWordById(wordId)
                if (word != null) {
                    currentWord = word
                    _wordData.value = word
                    loadWordDetails(word)
                } else {
                    _error.value = "Слово не знайдено"
                }
            } catch (e: Exception) {
                _error.value = "Помилка завантаження слова: ${e.message}"
            }
        }
    }

    fun loadWordDetails(vocabularyItem: VocabularyItem) {
        currentWord = vocabularyItem
        _wordData.value = vocabularyItem

        viewModelScope.launch {
            try {
                // Перевіряємо чи слово в улюблених
                val isFavorite = repository.isWordFavorite(vocabularyItem.id)
                _isFavorite.value = isFavorite

                // Завантажуємо інформацію про тему
                loadTopicInfo(vocabularyItem.topicId)

            } catch (e: Exception) {
                _error.value = "Помилка завантаження деталей: ${e.message}"
            }
        }
    }

    private suspend fun loadTopicInfo(topicId: String) {
        try {
            val topics = repository.getAllTopics()
            val topic = topics.find { it.id == topicId }
            _topicInfo.value = topic

            // Якщо знайшли тему, завантажуємо інформацію про розділ
            topic?.let { loadChapterInfo(it.chapterId) }

        } catch (e: Exception) {
            _error.value = "Помилка завантаження інформації про тему: ${e.message}"
        }
    }

    private suspend fun loadChapterInfo(chapterId: String) {
        try {
            val chapters = repository.getAllChapters()
            val chapter = chapters.find { it.id == chapterId }
            _chapterInfo.value = chapter

        } catch (e: Exception) {
            _error.value = "Помилка завантаження інформації про розділ: ${e.message}"
        }
    }

    fun toggleFavorite(vocabularyItem: VocabularyItem) {
        viewModelScope.launch {
            try {
                val currentFavoriteStatus = _isFavorite.value ?: false
                val newStatus = !currentFavoriteStatus

                val success = if (newStatus) {
                    repository.addToFavorites(vocabularyItem.id)
                } else {
                    repository.removeFromFavorites(vocabularyItem.id)
                }

                if (success) {
                    _isFavorite.value = newStatus
                } else {
                    _error.value = "Помилка оновлення улюблених"
                }

            } catch (e: Exception) {
                _error.value = "Помилка: ${e.message}"
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}