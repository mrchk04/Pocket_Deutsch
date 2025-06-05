package com.example.pocketdeutsch.ui.flashcards

import android.util.Log
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pocketdeutsch.data.model.FlashcardSetWithProgress
import com.example.pocketdeutsch.databinding.ActivityFlashcardSetsBinding
import com.example.pocketdeutsch.ui.components.BottomBarManager
import com.example.pocketdeutsch.ui.components.TopBarManager
import com.example.pocketdeutsch.ui.main.MainActivity
import com.example.pocketdeutsch.ui.profile.ProfileActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class FlashcardSetsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFlashcardSetsBinding
    private lateinit var viewModel: FlashcardSetsViewModel
    private lateinit var adapter: FlashcardSetsAdapter
    private lateinit var topBarManager: TopBarManager
    private lateinit var bottomBarManager: BottomBarManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFlashcardSetsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        topBarManager = TopBarManager(this)
        bottomBarManager = BottomBarManager(this)

        initializeViewModel()
        setupRecyclerView()
        observeData()
        setupRefreshListener()
        setupTopBarClickListeners()
        setupBottomBarNavigation()
        viewModel.loadUserData()
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

    private fun debugFirestore() {
        lifecycleScope.launch {
            try {
                Log.d("FlashcardSets", "🔍 Початок діагностики...")

                // Перевіряємо підключення до Firestore
                val firestore = FirebaseFirestore.getInstance()

                // Перевіряємо аутентифікацію
                val currentUser = FirebaseAuth.getInstance().currentUser
                Log.d("FlashcardSets", "👤 Current user: ${currentUser?.uid}")
                Log.d("FlashcardSets", "📧 User email: ${currentUser?.email}")

                // Перевіряємо набори флеш-карток
                val setsSnapshot = firestore.collection("flashcard_sets")
                    .limit(3)
                    .get()
                    .await()

                Log.d("FlashcardSets", "📚 Знайдено ${setsSnapshot.size()} наборів флеш-карток")

                setsSnapshot.documents.forEach { doc ->
                    val data = doc.data
                    Log.d("FlashcardSets", "📋 Набір ${doc.id}: ${data}")

                    // Перевіряємо флеш-картки для кожного набору
                    val flashcardsSnapshot = firestore.collection("flashcards")
                        .whereEqualTo("flashcard_set_id", doc.id)
                        .limit(3)
                        .get()
                        .await()

                    Log.d("FlashcardSets", "🃏 Флеш-карток в наборі ${doc.id}: ${flashcardsSnapshot.size()}")

                    flashcardsSnapshot.documents.forEach { flashcardDoc ->
                        Log.d("FlashcardSets", "   📄 Флеш-картка: ${flashcardDoc.data}")
                    }
                }

                // Перевіряємо теми
                val topicsSnapshot = firestore.collection("topics")
                    .limit(3)
                    .get()
                    .await()

                Log.d("FlashcardSets", "🎯 Знайдено ${topicsSnapshot.size()} тем")

                // Перевіряємо словникові одиниці
                val vocabSnapshot = firestore.collection("vocabulary_items")
                    .limit(3)
                    .get()
                    .await()

                Log.d("FlashcardSets", "📝 Знайдено ${vocabSnapshot.size()} словникових одиниць")

                Log.d("FlashcardSets", "✅ Діагностика завершена")

            } catch (e: Exception) {
                Log.e("FlashcardSets", "❌ Помилка діагностики", e)
            }
        }
    }

    // Додайте також цей метод для перевірки окремого набору
    private fun debugSpecificSet(setId: String) {
        lifecycleScope.launch {
            try {
                val firestore = FirebaseFirestore.getInstance()

                // Отримуємо конкретний набір
                val setDoc = firestore.collection("flashcard_sets")
                    .document(setId)
                    .get()
                    .await()

                Log.d("FlashcardSets", "🔍 Набір $setId існує: ${setDoc.exists()}")
                Log.d("FlashcardSets", "📋 Дані набору: ${setDoc.data}")

                // Шукаємо флеш-картки для цього набору
                val flashcards = firestore.collection("flashcards")
                    .whereEqualTo("flashcard_set_id", setId)
                    .get()
                    .await()

                Log.d("FlashcardSets", "🃏 Знайдено ${flashcards.size()} флеш-карток для набору $setId")

                flashcards.documents.forEachIndexed { index, doc ->
                    Log.d("FlashcardSets", "   $index: ${doc.data}")
                }

                // Також спробуємо з числовим ID (якщо в базі зберігається як число)
                val flashcardsNumeric = firestore.collection("flashcards")
                    .whereEqualTo("flashcard_set_id", setId.toIntOrNull() ?: setId)
                    .get()
                    .await()

                Log.d("FlashcardSets", "🔢 З числовим ID знайдено ${flashcardsNumeric.size()} флеш-карток")

            } catch (e: Exception) {
                Log.e("FlashcardSets", "❌ Помилка при перевірці набору $setId", e)
            }
        }
    }

    private fun initializeViewModel() {
        viewModel = ViewModelProvider(this)[FlashcardSetsViewModel::class.java]
    }

    private fun setupRecyclerView() {
        adapter = FlashcardSetsAdapter(
            onItemClick = { flashcardSet ->
                openFlashcardSetDetail(flashcardSet)
            },
            onStudyClick = { flashcardSet ->
                startStudying(flashcardSet)
            }
        )

        binding.flashcardSetsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@FlashcardSetsActivity)
            adapter = this@FlashcardSetsActivity.adapter
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

        // Спостерігаємо за списком наборів
        viewModel.flashcardSets.observe(this) { flashcardSets ->
            adapter.submitList(flashcardSets)
            updateEmptyState(flashcardSets.isEmpty())
        }

        // Спостерігаємо за статистикою користувача
        viewModel.userStatistics.observe(this) { statistics ->
            updateUserStatistics(statistics)
        }

        // Спостерігаємо за станом завантаження
        viewModel.isLoading.observe(this) { isLoading ->
            binding.loadingProgress.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.flashcardSetsRecyclerView.visibility = if (isLoading) View.GONE else View.VISIBLE
        }

        // Спостерігаємо за помилками
        viewModel.error.observe(this) { error ->
            error?.let {
                showError(it)
                viewModel.clearError()
            }
        }
    }

    private fun updateUserStatistics(statistics: com.example.pocketdeutsch.data.model.UserStatistics) {
        binding.apply {
            totalStudied.text = statistics.learnedWordsCount.toString()
            totalMastered.text = statistics.flashcardsCompleted.toString()
            currentStreak.text = statistics.studyDaysStreak.toString()
        }
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        binding.emptyState.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.flashcardSetsRecyclerView.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }

    private fun setupRefreshListener() {
        // Якщо у вас є SwipeRefreshLayout, можна додати тут
        // Або додати pull-to-refresh функціональність
    }

    private fun openFlashcardSetDetail(flashcardSet: FlashcardSetWithProgress) {
        Log.d("FlashcardSetsActivity", "Opening detail for set: ${flashcardSet.flashcardSet.title}")
        Log.d("FlashcardSetsActivity", "Set ID: ${flashcardSet.flashcardSet.id}")

        val intent = Intent(this, FlashcardSetDetailActivity::class.java).apply {
            putExtra("flashcard_set_id", flashcardSet.flashcardSet.id)
            putExtra("flashcard_set_title", flashcardSet.flashcardSet.title)
            putExtra("topic_title", flashcardSet.topic?.title)
        }
        startActivityForResult(intent, REQUEST_CODE_DETAIL)
    }

    private fun startStudying(flashcardSet: FlashcardSetWithProgress) {
        if (flashcardSet.totalCards == 0) {
            Toast.makeText(this, "В цьому наборі немає карток для вивчення", Toast.LENGTH_SHORT).show()
            return
        }

        val intent = Intent(this, FlashcardStudyActivity::class.java).apply {
            putExtra("flashcard_set_id", flashcardSet.flashcardSet.id)
            putExtra("flashcard_set_title", flashcardSet.flashcardSet.title)
            putExtra("study_mode", "all") // "all" або "new"
        }
        startActivityForResult(intent, REQUEST_CODE_STUDY)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            REQUEST_CODE_STUDY -> {
                if (resultCode == RESULT_OK) {
                    // Оновлюємо дані після завершення навчання
                    viewModel.refreshData()
                    viewModel.updateStatistics()
                }
            }
            REQUEST_CODE_DETAIL -> {
                if (resultCode == RESULT_OK) {
                    // Оновлюємо дані після повернення з деталей
                    viewModel.refreshData()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Оновлюємо дані при поверненні до активності
        viewModel.refreshData()
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    companion object {
        private const val REQUEST_CODE_STUDY = 1001
        private const val REQUEST_CODE_DETAIL = 1002
    }
}