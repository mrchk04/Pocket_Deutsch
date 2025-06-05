package com.example.pocketdeutsch.ui.flashcards

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pocketdeutsch.data.model.FlashcardWithVocabulary
import com.example.pocketdeutsch.data.repository.FlashcardRepository
import com.example.pocketdeutsch.databinding.ActivityFlashcardSetDetailBinding
import com.example.pocketdeutsch.ui.components.BottomBarManager
import com.example.pocketdeutsch.ui.components.TopBarManager
import com.example.pocketdeutsch.ui.main.MainActivity
import com.example.pocketdeutsch.ui.profile.ProfileActivity

class FlashcardSetDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFlashcardSetDetailBinding
    private lateinit var viewModel: FlashcardSetDetailViewModel
    private lateinit var adapter: FlashcardDetailAdapter
    private lateinit var topBarManager: TopBarManager
    private lateinit var bottomBarManager: BottomBarManager

    private var flashcardSetId: String = ""
    private var flashcardSetTitle: String = ""
    private var topicTitle: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFlashcardSetDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        topBarManager = TopBarManager(this)
        bottomBarManager = BottomBarManager(this)

        getIntentData()
        initializeViewModel()
        setupRecyclerView()
        setupClickListeners()
        observeData()
        setupTopBarClickListeners()
        setupBottomBarNavigation()
        viewModel.loadUserData()

        // Завантажуємо дані
        viewModel.loadFlashcardSet(flashcardSetId)
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

    private fun setupBottomBarNavigation() {
        // Налаштовуємо навігацію для bottom bar
        bottomBarManager.setupNavigation(
            homepageActivityClass = MainActivity::class.java,
        )

        bottomBarManager.setWiederholungClickListener {
            // TODO: Navigate to Wiederholung activity
            Toast.makeText(this, "Повторення - TODO", Toast.LENGTH_SHORT).show()
        }

        bottomBarManager.setInteressantClickListener {
            // TODO: Navigate to Interessant activity
            Toast.makeText(this, "Цікаве - TODO", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getIntentData() {
        flashcardSetId = intent.getStringExtra("flashcard_set_id") ?: ""
        flashcardSetTitle = intent.getStringExtra("flashcard_set_title") ?: ""
        topicTitle = intent.getStringExtra("topic_title")

        if (flashcardSetId.isEmpty()) {
            Toast.makeText(this, "Помилка: не вказано ID набору", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
    }

    private fun initializeViewModel() {
        val repository = FlashcardRepository()
        val factory = FlashcardViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[FlashcardSetDetailViewModel::class.java]
    }

    private fun setupRecyclerView() {
        adapter = FlashcardDetailAdapter { flashcard ->
            openFlashcardDetail(flashcard)
        }

        binding.flashcardsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@FlashcardSetDetailActivity)
            adapter = this@FlashcardSetDetailActivity.adapter
        }
    }

    private fun setupClickListeners() {
        binding.apply {
            btnStudyAll.setOnClickListener {
                startStudying("all")
            }

            btnStudyNew.setOnClickListener {
                startStudying("new")
            }
        }
    }

    private fun observeData() {

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

        // Спостерігаємо за набором флеш-карток
        viewModel.flashcardSet.observe(this) { flashcardSet ->
            flashcardSet?.let {
                binding.setTitle.text = it.title
            }
        }

        // Спостерігаємо за темою
        viewModel.topic.observe(this) { topic ->
            // Тема вже відображається в заголовку через Intent
        }

        // Спостерігаємо за списком флеш-карток
        viewModel.flashcards.observe(this) { flashcards ->
            adapter.submitList(flashcards)
            updateButtonStates(flashcards)
        }

        // Спостерігаємо за статистикою
        viewModel.statistics.observe(this) { statistics ->
            updateStatistics(statistics)
        }

        // Спостерігаємо за станом завантаження
        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBarLoading.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.flashcardsRecyclerView.visibility = if (isLoading) View.GONE else View.VISIBLE
        }

        // Спостерігаємо за помилками
        viewModel.error.observe(this) { error ->
            error?.let {
                showError(it)
                viewModel.clearError()
            }
        }
    }

    private fun updateStatistics(statistics: com.example.pocketdeutsch.data.model.FlashcardSetStatistics) {
        binding.apply {
            totalCardsNumber.text = statistics.totalCards.toString()
            studiedCardsNumber.text = statistics.studiedCards.toString()
            averageConfidenceNumber.text = String.format("%.1f", statistics.averageConfidence)

            progressText.text = "Прогрес: ${statistics.progressPercentage}%"
            progressBar.progress = statistics.progressPercentage
        }
    }

    private fun updateButtonStates(flashcards: List<FlashcardWithVocabulary>) {
        val newFlashcards = viewModel.getNewFlashcards()

        binding.apply {
            // Вимикаємо кнопку "Тільки нові", якщо немає нових карток
            btnStudyNew.isEnabled = newFlashcards.isNotEmpty()
            btnStudyNew.alpha = if (newFlashcards.isNotEmpty()) 1.0f else 0.5f

            // Вимикаємо кнопку "Вчити всі", якщо немає карток взагалі
            btnStudyAll.isEnabled = flashcards.isNotEmpty()
            btnStudyAll.alpha = if (flashcards.isNotEmpty()) 1.0f else 0.5f
        }
    }

    private fun startStudying(mode: String) {
        val flashcards = when (mode) {
            "new" -> viewModel.getNewFlashcards()
            "all" -> viewModel.getAllFlashcards()
            else -> viewModel.getAllFlashcards()
        }

        if (flashcards.isEmpty()) {
            val message = when (mode) {
                "new" -> "Немає нових карток для вивчення"
                else -> "Немає карток для вивчення"
            }
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            return
        }

        val intent = Intent(this, FlashcardStudyActivity::class.java).apply {
            putExtra("flashcard_set_id", flashcardSetId)
            putExtra("flashcard_set_title", flashcardSetTitle)
            putExtra("study_mode", mode)
        }
        startActivityForResult(intent, REQUEST_CODE_STUDY)
    }

    private fun openFlashcardDetail(flashcard: FlashcardWithVocabulary) {
        // Тут можна відкрити детальний вигляд окремої картки
        // Або одразу почати вивчення з цієї картки
        val intent = Intent(this, FlashcardStudyActivity::class.java).apply {
            putExtra("flashcard_set_id", flashcardSetId)
            putExtra("flashcard_set_title", flashcardSetTitle)
            putExtra("start_flashcard_id", flashcard.flashcard.id)
            putExtra("study_mode", "all")
        }
        startActivityForResult(intent, REQUEST_CODE_STUDY)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_STUDY && resultCode == RESULT_OK) {
            // Оновлюємо дані після завершення навчання
            viewModel.refreshData(flashcardSetId)
        }
    }

    override fun onResume() {
        super.onResume()
        // Оновлюємо дані при поверненні до активності
        viewModel.refreshData(flashcardSetId)
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    companion object {
        private const val REQUEST_CODE_STUDY = 1001
    }
}