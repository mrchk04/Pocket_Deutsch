package com.example.pocketdeutsch.ui.main

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
import androidx.lifecycle.lifecycleScope
import com.example.pocketdeutsch.data.model.DataHealthStatus
import com.example.pocketdeutsch.databinding.ActivityMainBinding
import com.example.pocketdeutsch.ui.auth.LoginActivity
import com.example.pocketdeutsch.ui.components.BottomBarManager
import com.example.pocketdeutsch.ui.components.BottomBarTab
import com.example.pocketdeutsch.ui.components.TopBarManager
import com.example.pocketdeutsch.ui.profile.ProfileActivity
import com.example.pocketdeutsch.ui.vocabulary.VocabularyActivity
import com.example.pocketdeutsch.utils.DataInitializationManager
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel
    private lateinit var topBarManager: TopBarManager
    private lateinit var bottomBarManager: BottomBarManager
    private lateinit var dataInitializationManager: DataInitializationManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Ініціалізація ViewModel з Factory
        val factory = MainViewModelFactory(application)
        viewModel = ViewModelProvider(this, factory)[MainViewModel::class.java]

        topBarManager = TopBarManager(this)
        bottomBarManager = BottomBarManager(this)
        dataInitializationManager = DataInitializationManager(this)

        setupObservers()
        setupClickListeners()
        setupTopBarClickListeners()
        setupBottomBarNavigation()

        // Виділяємо активну вкладку (Homepage)
        bottomBarManager.setActiveTab(BottomBarTab.HOMEPAGE)

        // Load initial data
        viewModel.loadUserData()
//        viewModel.loadWordOfTheDay()
        Log.d("Firebase", "Current user: ${FirebaseAuth.getInstance().currentUser?.email}")
        Log.d("Firebase", "Current user UID: ${FirebaseAuth.getInstance().currentUser?.uid}")
    }

    private fun setupBottomBarNavigation() {
        // Налаштовуємо навігацію для bottom bar
        bottomBarManager.setupNavigation(
            homepageActivityClass = MainActivity::class.java,
        )

        bottomBarManager.setHomepageClickListener {
            refreshData()
        }

        bottomBarManager.setWiederholungClickListener {
            // TODO: Navigate to Wiederholung activity
            Toast.makeText(this, "Wiederholung - TODO", Toast.LENGTH_SHORT).show()
        }

        bottomBarManager.setInteressantClickListener {
            // TODO: Navigate to Interessant activity
            Toast.makeText(this, "Interessant - TODO", Toast.LENGTH_SHORT).show()
        }
    }

    private fun refreshData() {
        viewModel.loadUserData()
        viewModel.loadWordOfTheDay()
        Toast.makeText(this, "Daten aktualisiert", Toast.LENGTH_SHORT).show()
    }

    private fun setupObservers() {
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

        viewModel.wordOfTheDay.observe(this, Observer { vocabularyItem ->
            if (vocabularyItem != null) {
                binding.germanWord.text = vocabularyItem.wordDe
                binding.ukrainianTranslation.text = vocabularyItem.translation
            }
        })

        viewModel.isLoading.observe(this, Observer { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        })

        viewModel.error.observe(this, Observer { errorMessage ->
            if (errorMessage.isNotEmpty()) {
                Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
            }
        })

        viewModel.userNotAuthenticated.observe(this, Observer { notAuthenticated ->
            if (notAuthenticated) {
                navigateToAuth()
            }
        })
    }

    private fun setupClickListeners() {
        binding.wordOfDayCard.setOnClickListener {
            // TODO: Navigate to word details
            Toast.makeText(this, "Word details - TODO", Toast.LENGTH_SHORT).show()
        }

        binding.dictionaryCard.setOnClickListener {
            navigateToVocabulary()
        }

        binding.flashcardsCard.setOnClickListener {
            // TODO: Navigate to flashcards
            Toast.makeText(this, "Flashcards - TODO", Toast.LENGTH_SHORT).show()
        }

        binding.grammarCard.setOnClickListener {
            // TODO: Navigate to grammar
            Toast.makeText(this, "Grammar - TODO", Toast.LENGTH_SHORT).show()
        }

        binding.myBooksCard.setOnClickListener {
            // TODO: Navigate to books
            Toast.makeText(this, "My Books - TODO", Toast.LENGTH_SHORT).show()
        }
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

    private fun navigateToAuth() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
    private fun navigateToProfile() {
        val intent = Intent(this, ProfileActivity::class.java)
        startActivity(intent)
    }
    private fun navigateToVocabulary() {
        try {
            val intent = Intent(this, VocabularyActivity::class.java)
            startActivity(intent)
//            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        } catch (e: Exception) {
            Toast.makeText(this, "Помилка відкриття словника", Toast.LENGTH_SHORT).show()
        }
    }

    private fun initializeApp() {
        lifecycleScope.launch {
            try {
                val isInitialized = dataInitializationManager.initializeAppData()

                if (isInitialized) {
                    // Перевіряємо здоров'я даних
                    val healthStatus = dataInitializationManager.checkDataHealth()

                    if (healthStatus.isHealthy) {
                        showDataStats(healthStatus)
                    } else {
                        showDataWarning(healthStatus)
                    }
                } else {
                    showDataImportInstructions()
                }

            } catch (e: Exception) {
                Toast.makeText(
                    this@MainActivity,
                    "Помилка ініціалізації: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun showDataStats(healthStatus: DataHealthStatus) {
        val message = """
            Словник завантажений! 📚
            
            📖 Слова: ${healthStatus.wordsCount}
            🏷️ Теми: ${healthStatus.topicsCount}  
            📑 Розділи: ${healthStatus.chaptersCount}
            📚 Підручники: ${healthStatus.textbooksCount}
        """.trimIndent()

        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun showDataWarning(healthStatus: DataHealthStatus) {
        val message = if (healthStatus.error != null) {
            "Помилка завантаження даних: ${healthStatus.error}"
        } else {
            "Недостатньо даних у словнику (${healthStatus.wordsCount} слів)"
        }

        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun showDataImportInstructions() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Необхідно імпортувати дані")
            .setMessage(
                """
                Схоже, що дані ще не імпортовані в Firestore.
                
                Щоб імпортувати дані:
                1. Запустіть Node.js скрипт import-data.js
                2. Перезапустіть додаток
                
                Або зверніться до розробника за допомогою.
                """.trimIndent()
            )
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .setNeutralButton("Спробувати знову") { _, _ ->
                initializeApp()
            }
            .show()
    }

    override fun onResume() {
        super.onResume()
        // Перевіряємо дані при поверненні до активності
        lifecycleScope.launch {
            val healthStatus = dataInitializationManager.checkDataHealth()
            if (healthStatus.isHealthy && healthStatus.wordsCount > 0) {
                // Дані в порядку, нічого робити не треба
            }
        }
    }

}