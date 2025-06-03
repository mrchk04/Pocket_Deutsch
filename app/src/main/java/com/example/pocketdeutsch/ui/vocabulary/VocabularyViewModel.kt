package com.example.pocketdeutsch.ui.vocabulary

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.pocketdeutsch.data.model.User
import com.example.pocketdeutsch.data.model.VocabularyItem
import com.example.pocketdeutsch.data.model.VocabularyStats
import com.example.pocketdeutsch.data.repository.UserRepository
import com.example.pocketdeutsch.data.repository.VocabularyRepository
import kotlinx.coroutines.launch

class VocabularyViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = VocabularyRepository(application)
    private val userRepository = UserRepository()

    private val _userData = MutableLiveData<User?>()
    val userData: LiveData<User?> = _userData

    private val _vocabularyList = MutableLiveData<List<VocabularyAdapter.VocabularyItemWithFavorite>>()
    val vocabularyList: LiveData<List<VocabularyAdapter.VocabularyItemWithFavorite>> = _vocabularyList

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _isShowingFavorites = MutableLiveData<Boolean>(false)
    val isShowingFavorites: LiveData<Boolean> = _isShowingFavorites

    private val _searchQuery = MutableLiveData<String>("")
    val searchQuery: LiveData<String> = _searchQuery

    private val _selectedTopic = MutableLiveData<String?>()
    val selectedTopic: LiveData<String?> = _selectedTopic

    private val _selectedChapter = MutableLiveData<String?>()
    val selectedChapter: LiveData<String?> = _selectedChapter

    private val _selectedPartOfSpeech = MutableLiveData<String?>()
    val selectedPartOfSpeech: LiveData<String?> = _selectedPartOfSpeech

    private var allWords: List<VocabularyItem> = emptyList()
    private var favoriteWordIds: Set<String> = emptySet()

    private val _userNotAuthenticated = MutableLiveData<Boolean>()
    val userNotAuthenticated: LiveData<Boolean> = _userNotAuthenticated

    init {
        loadAllWords()
    }

    fun loadAllWords() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                allWords = repository.getAllWords()
                loadFavoriteWordIds()
                applyFilters()
            } catch (e: Exception) {
                _error.value = "Помилка завантаження словника: ${e.message}"
                // Логування для debug
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadFavoriteWords() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val favoriteWords = repository.getFavoriteWords()
                val favoriteWordIds = favoriteWords.map { it.id }.toSet()

                val wordsWithFavorites = favoriteWords.map { word ->
                    VocabularyAdapter.VocabularyItemWithFavorite(
                        vocabularyItem = word,
                        favorite = true
                    )
                }

                _vocabularyList.value = wordsWithFavorites
                this@VocabularyViewModel.favoriteWordIds = favoriteWordIds

            } catch (e: Exception) {
                _error.value = "Помилка завантаження улюблених слів: ${e.message}"
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun loadFavoriteWordIds() {
        try {
            val favoriteWords = repository.getFavoriteWords()
            favoriteWordIds = favoriteWords.map { it.id }.toSet()
        } catch (e: Exception) {
            // Логування помилки, але не блокуємо основний функціонал
            e.printStackTrace()
        }
    }

    fun switchToAllWords() {
        _isShowingFavorites.value = false
        loadAllWords()
    }

    fun switchToFavorites() {
        _isShowingFavorites.value = true
        loadFavoriteWords()
    }

    fun searchWords(query: String) {
        _searchQuery.value = query

        if (query.isBlank()) {
            if (_isShowingFavorites.value == true) {
                loadFavoriteWords()
            } else {
                applyFilters()
            }
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val searchResults = repository.searchWords(query)
                val wordsWithFavorites = searchResults.map { word ->
                    VocabularyAdapter.VocabularyItemWithFavorite(
                        vocabularyItem = word,
                        favorite = favoriteWordIds.contains(word.id)
                    )
                }
                _vocabularyList.value = wordsWithFavorites
            } catch (e: Exception) {
                _error.value = "Помилка пошуку: ${e.message}"
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun filterByTopic(topicId: String?) {
        _selectedTopic.value = topicId
        applyFilters()
    }

    fun filterByChapter(chapterId: String?) {
        _selectedChapter.value = chapterId
        applyFilters()
    }

    fun filterByPartOfSpeech(partOfSpeech: String?) {
        _selectedPartOfSpeech.value = partOfSpeech
        applyFilters()
    }

    private fun applyFilters() {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                var filteredWords = allWords

                // Фільтр по розділу (має пріоритет над темою)
                _selectedChapter.value?.let { chapterId ->
                    filteredWords = repository.getWordsByChapter(chapterId)
                }

                // Фільтр по темі (якщо не вибрано розділ)
                if (_selectedChapter.value == null) {
                    _selectedTopic.value?.let { topicId ->
                        filteredWords = filteredWords.filter { it.topicId == topicId }
                    }
                }

                // Фільтр по частині мови
                _selectedPartOfSpeech.value?.let { partOfSpeech ->
                    filteredWords = filteredWords.filter { it.partOfSpeech == partOfSpeech }
                }

                val wordsWithFavorites = filteredWords.map { word ->
                    VocabularyAdapter.VocabularyItemWithFavorite(
                        vocabularyItem = word,
                        favorite = favoriteWordIds.contains(word.id)
                    )
                }

                _vocabularyList.value = wordsWithFavorites
            } catch (e: Exception) {
                _error.value = "Помилка фільтрації: ${e.message}"
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun toggleFavorite(vocabularyItem: VocabularyItem, favorite: Boolean) {

        Log.d("VocabularyViewModel", "🔍 toggleFavorite called")
        Log.d("VocabularyViewModel", "🔍 Word: '${vocabularyItem.wordDe}'")
        Log.d("VocabularyViewModel", "🔍 Word ID: '${vocabularyItem.id}'")
        Log.d("VocabularyViewModel", "🔍 Setting favorite to: $favorite")

        viewModelScope.launch {
            try {
                val success = if (favorite) {
                    repository.addToFavorites(vocabularyItem.id)
                } else {
                    repository.removeFromFavorites(vocabularyItem.id)
                }

                if (success) {
                    // Оновлюємо локальний список улюблених
                    favoriteWordIds = if (favorite) {
                        favoriteWordIds + vocabularyItem.id
                    } else {
                        favoriteWordIds - vocabularyItem.id
                    }

                    // Оновлюємо поточний список
                    if (_isShowingFavorites.value == true) {
                        loadFavoriteWords()
                    } else {
                        updateCurrentListFavoriteStatus(vocabularyItem.id, favorite)
                    }
                } else {
                    _error.value = "Помилка оновлення улюблених"
                }
            } catch (e: Exception) {
                _error.value = "Помилка: ${e.message}"
                e.printStackTrace()
            }
        }
    }

    private fun updateCurrentListFavoriteStatus(wordId: String, favorite: Boolean) {
        val currentList = _vocabularyList.value ?: return
        val updatedList = currentList.map { item ->
            if (item.vocabularyItem.id == wordId) {
                item.copy(favorite = favorite)
            } else {
                item
            }
        }
        _vocabularyList.value = updatedList
    }

    fun clearError() {
        _error.value = null
    }

    fun clearAllFilters() {
        _selectedTopic.value = null
        _selectedChapter.value = null
        _selectedPartOfSpeech.value = null
        applyFilters()
    }

    fun refreshData() {
        if (_isShowingFavorites.value == true) {
            loadFavoriteWords()
        } else {
            loadAllWords()
        }
    }

    // Методи для отримання інформації про фільтри
    fun getActiveFiltersCount(): Int {
        var count = 0
        if (_selectedTopic.value != null) count++
        if (_selectedChapter.value != null) count++
        if (_selectedPartOfSpeech.value != null) count++
        return count
    }

    fun hasActiveFilters(): Boolean {
        return getActiveFiltersCount() > 0
    }

    // Статистика
    fun getVocabularyStats(): VocabularyStats {
        val totalWords = allWords.size
        val favoriteCount = favoriteWordIds.size
        val currentDisplayed = _vocabularyList.value?.size ?: 0

        return VocabularyStats(
            totalWords = totalWords,
            favoriteWords = favoriteCount,
            displayedWords = currentDisplayed,
            hasFilters = hasActiveFilters()
        )
    }

    fun loadUserData() {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                if (!userRepository.isUserAuthenticated()) {
                    _userNotAuthenticated.value = true
                    return@launch
                }

                val user = userRepository.getCurrentUser()
                if (user != null) {
                    _userData.value = user
                } else {
                    _error.value = "Неможливо завантажити дані користувача."
                }

            } catch (e: Exception) {
                _error.value = "Помилка під час завантаження даних користувача: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}