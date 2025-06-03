package com.example.pocketdeutsch.ui.vocabulary

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pocketdeutsch.databinding.ActivityVocabularyBinding
import com.example.pocketdeutsch.R
import com.example.pocketdeutsch.data.model.VocabularyItem
import com.example.pocketdeutsch.ui.components.BottomBarManager
import com.example.pocketdeutsch.ui.components.TopBarManager
import com.example.pocketdeutsch.ui.profile.ProfileActivity
import com.example.pocketdeutsch.ui.vocabulary.utils.CustomFilterDialogHelper
import com.example.pocketdeutsch.ui.wordDetails.WordDetailsActivity

class VocabularyActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVocabularyBinding
    private val viewModel: VocabularyViewModel by viewModels()
    private lateinit var vocabularyAdapter: VocabularyAdapter
    private lateinit var filterDialogHelper: CustomFilterDialogHelper
    private lateinit var topBarManager: TopBarManager
    private lateinit var bottomBarManager: BottomBarManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityVocabularyBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        topBarManager = TopBarManager(this)
        bottomBarManager = BottomBarManager(this)

        viewModel.loadUserData()

        initializeHelpers()
        setupRecyclerView()
        setupClickListeners()
        setupSearchView()
        observeViewModel()
        setupFilterButtons()
        setupTopBarClickListeners()
    }

    private fun initializeHelpers() {
//        audioManagerHelper = AudioManagerHelper(this)
        filterDialogHelper = CustomFilterDialogHelper(this, lifecycleScope)
    }

    private fun setupRecyclerView() {
        vocabularyAdapter = VocabularyAdapter(
            onItemClick = { vocabularyItem ->
                // Можна додати детальний перегляд слова
                showWordDetails(vocabularyItem)
            },
            onFavoriteClick = { vocabularyItem, isFavorite ->
                viewModel.toggleFavorite(vocabularyItem, isFavorite)
            },
//            onAudioClick = { vocabularyItem ->
//                audioManagerHelper.playAudio(vocabularyItem.audioUrl) { error ->
//                    Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
//                }
//            }
        )

        binding.vocabularyRecyclerView.apply {
            adapter = vocabularyAdapter
            layoutManager = LinearLayoutManager(this@VocabularyActivity)
            setHasFixedSize(true)
        }
    }

    private fun setupClickListeners() {
        // Кнопки переключення між всіма словами та улюбленими
        binding.btnAllWords.setOnClickListener {
            switchToAllWordsMode()
            viewModel.switchToAllWords()
        }

        binding.btnFavorites.setOnClickListener {
            switchToFavoritesMode()
            viewModel.switchToFavorites()
        }

        // Кнопки фільтрів
        binding.btnFilterTopic.setOnClickListener {
            showTopicFilterDialog()
        }

        binding.btnFilterPos.setOnClickListener {
            showPartOfSpeechFilterDialog()
        }
    }

    private fun setupSearchView() {
        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s?.toString() ?: ""
                viewModel.searchWords(query)
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupFilterButtons() {
        // Додаємо індикатор активних фільтрів
        updateFilterButtonsState()
    }

    private fun observeViewModel() {

        viewModel.userData.observe(this, Observer { user ->
            if (user != null) {
                // Оновлюємо привітання в top bar через менеджер
                topBarManager.updateUserGreeting(user)

                Log.d("MainActivity", "User data loaded: ${user.firstName}")
            } else {
                Log.d("MainActivity", "User data is null")
                topBarManager.updateUserGreeting("Gast") // Гость
            }
        })

        viewModel.vocabularyList.observe(this) { wordsList ->
            vocabularyAdapter.submitList(wordsList)
            updateEmptyState(wordsList.isEmpty())
            updateStats()
        }

        viewModel.isLoading.observe(this) { isLoading ->
            // Можна додати ProgressBar
            binding.vocabularyRecyclerView.visibility = if (isLoading) View.GONE else View.VISIBLE
        }

        viewModel.error.observe(this) { error ->
            error?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
                viewModel.clearError()
            }
        }

        viewModel.isShowingFavorites.observe(this) { isShowingFavorites ->
            updateModeButtons(isShowingFavorites)
        }

        // Спостерігаємо за фільтрами
        viewModel.selectedTopic.observe(this) { updateFilterButtonsState() }
        viewModel.selectedChapter.observe(this) { updateFilterButtonsState() }
        viewModel.selectedPartOfSpeech.observe(this) { updateFilterButtonsState() }
    }

    private fun switchToAllWordsMode() {
        binding.btnAllWords.apply {
            background = ContextCompat.getDrawable(this@VocabularyActivity, R.drawable.tab_button)
            setTextColor(ContextCompat.getColor(this@VocabularyActivity, R.color.black))
            setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_book_open1, 0, 0, 0)
        }

        binding.btnFavorites.apply {
            background = ContextCompat.getDrawable(this@VocabularyActivity, R.drawable.tab_button1)
            setTextColor(ContextCompat.getColor(this@VocabularyActivity, R.color.gray5))
            setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_book_open, 0, 0, 0)
        }
    }

    private fun switchToFavoritesMode() {
        binding.btnFavorites.apply {
            background = ContextCompat.getDrawable(this@VocabularyActivity, R.drawable.tab_button)
            setTextColor(ContextCompat.getColor(this@VocabularyActivity, R.color.black))
            setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_book_open1, 0, 0, 0)
        }

        binding.btnAllWords.apply {
            background = ContextCompat.getDrawable(this@VocabularyActivity, R.drawable.tab_button1)
            setTextColor(ContextCompat.getColor(this@VocabularyActivity, R.color.gray5))
            setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_book_open, 0, 0, 0)
        }
    }

    private fun updateModeButtons(isShowingFavorites: Boolean) {
        if (isShowingFavorites) {
            switchToFavoritesMode()
        } else {
            switchToAllWordsMode()
        }
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        if (isEmpty) {
            binding.emptyState.visibility = View.VISIBLE
            binding.vocabularyRecyclerView.visibility = View.GONE

            val message = when {
                viewModel.isShowingFavorites.value == true -> "У вас немає улюблених слів"
                !binding.searchEditText.text.isNullOrBlank() -> "Нічого не знайдено за запитом"
                viewModel.hasActiveFilters() -> "Немає слів за обраними фільтрами"
                else -> "Словник порожній"
            }
            binding.emptyMessage.text = message
        } else {
            binding.emptyState.visibility = View.GONE
            binding.vocabularyRecyclerView.visibility = View.VISIBLE
        }
    }

    private fun updateFilterButtonsState() {
        val activeFilters = viewModel.getActiveFiltersCount()

        // Оновлюємо текст кнопок фільтрів
        binding.btnFilterTopic.text = if (viewModel.selectedTopic.value != null) {
            "Тема ✓"
        } else {
            "За темою"
        }

        binding.btnFilterPos.text = if (viewModel.selectedPartOfSpeech.value != null) {
            "Частина мови ✓"
        } else {
            "Частини мови"
        }

        // Показуємо кількість активних фільтрів
        if (activeFilters > 0) {
            binding.vocabularyTitle.text = "Словник ($activeFilters фільтрів)"
        } else {
            binding.vocabularyTitle.text = "Словник"
        }
    }

    private fun updateStats() {
        val stats = viewModel.getVocabularyStats()
        // Можна додати відображення статистики у футері
    }

    private fun showTopicFilterDialog() {
        filterDialogHelper.showTopicFilterDialog(
            onTopicSelected = { topicId ->
                viewModel.filterByTopic(topicId)
            },
            onFilterCleared = {
                viewModel.filterByTopic(null)
            }
        )
    }

    private fun showPartOfSpeechFilterDialog() {
        filterDialogHelper.showPartOfSpeechFilterDialog(
            onPartOfSpeechSelected = { partOfSpeech ->
                viewModel.filterByPartOfSpeech(partOfSpeech)
            },
            onFilterCleared = {
                viewModel.filterByPartOfSpeech(null)
            }
        )
    }

    private fun showChapterFilterDialog() {
        filterDialogHelper.showChapterFilterDialog(
            onChapterSelected = { chapterId ->
                viewModel.filterByChapter(chapterId)
            },
            onFilterCleared = {
                viewModel.filterByChapter(null)
            }
        )
    }

    private fun showWordDetails(vocabularyItem: VocabularyItem) {
        val intent = WordDetailsActivity.Companion.newIntent(this, vocabularyItem.id)
        startActivity(intent)
    }

    private fun setupTopBarClickListeners() {
        // Клік по профільній іконці
        topBarManager.setProfileClickListener {
            navigateToProfile()
//            Toast.makeText(this, "Profile - TODO", Toast.LENGTH_SHORT).show()
        }

        // Клік по привітанню (опціонально)
        topBarManager.setGreetingClickListener {
            // TODO: Show user menu or profile
            Toast.makeText(this, "User menu - TODO", Toast.LENGTH_SHORT).show()
        }
    }
    private fun navigateToProfile() {
        val intent = Intent(this, ProfileActivity::class.java)
        startActivity(intent)
    }

//    override fun onDestroy() {
//        super.onDestroy()
//        audioManagerHelper.release()
//    }
//
//    override fun onPause() {
//        super.onPause()
//        audioManagerHelper.pauseCurrentAudio()
//    }

//    override fun onBackPressed() {
//        super.onBackPressed()
//        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
//    }
}