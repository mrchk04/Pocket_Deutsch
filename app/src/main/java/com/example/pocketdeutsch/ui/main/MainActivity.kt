package com.example.pocketdeutsch.ui.main

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.pocketdeutsch.databinding.ActivityMainBinding
import com.example.pocketdeutsch.ui.auth.LoginActivity
import com.example.pocketdeutsch.ui.components.TopBarManager
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel
    private lateinit var topBarManager: TopBarManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()

        // Ініціалізація ViewModel з Factory
        val factory = MainViewModelFactory(application)
        viewModel = ViewModelProvider(this, factory)[MainViewModel::class.java]

        topBarManager = TopBarManager(this)

        setupObservers()
        setupClickListeners()
        setupTopBarClickListeners()

        // Load initial data
        viewModel.loadUserData()
//        viewModel.loadWordOfTheDay()
        Log.d("Firebase", "Current user: ${FirebaseAuth.getInstance().currentUser?.email}")
        Log.d("Firebase", "Current user UID: ${FirebaseAuth.getInstance().currentUser?.uid}")
    }

    private fun setupObservers() {
//        viewModel.userData.observe(this, Observer { user ->
//            if (user != null) {
//                binding.greeting.text = "Hallo, ${user.firstName}"
//            }
//        })

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
            // TODO: Navigate to dictionary
            Toast.makeText(this, "Dictionary - TODO", Toast.LENGTH_SHORT).show()
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
            // TODO: Navigate to profile
            Toast.makeText(this, "Profile - TODO", Toast.LENGTH_SHORT).show()
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
}