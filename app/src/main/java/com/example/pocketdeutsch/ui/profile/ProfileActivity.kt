package com.example.pocketdeutsch.ui.profile

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.pocketdeutsch.databinding.ActivityProfileBinding
import com.example.pocketdeutsch.ui.auth.LoginActivity
import com.example.pocketdeutsch.ui.components.BottomBarManager
import com.example.pocketdeutsch.ui.components.TopBarManager
import com.example.pocketdeutsch.ui.main.MainActivity

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var viewModel: ProfileViewModel
    private lateinit var topBarManager: TopBarManager
    private lateinit var bottomBarManager: BottomBarManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        /// Ініціалізація ViewModel
        val factory = ProfileViewModelFactory(application)
        viewModel = ViewModelProvider(this, factory)[ProfileViewModel::class.java]

        topBarManager = TopBarManager(this)
        bottomBarManager = BottomBarManager(this)

        setupObservers()
        setupClickListeners()
        setupBottomBarNavigation()

        // Завантаження даних
        viewModel.loadUserData()
        viewModel.loadUserStatistics()
    }

    private fun setupBottomBarNavigation() {
        // Налаштування навігації bottom bar
        bottomBarManager.setupNavigation(
            homepageActivityClass = MainActivity::class.java,
            wiederholungActivityClass = null, // TODO: Додайте клас активності для повторення
            interessantActivityClass = null   // TODO: Додайте клас активності для цікавого
        )

        // Альтернативний варіант з власними обробниками
        // bottomBarManager.setHomepageClickListener {
        //     // Вже на головній сторінці, нічого не робимо
        //     Toast.makeText(this, "Ви вже на головній сторінці", Toast.LENGTH_SHORT).show()
        // }
    }

    private fun setupObservers() {
        // Спостереження за даними користувача
        viewModel.userData.observe(this, Observer { user ->
            if (user != null) {
                binding.userFullName.text = "${user.firstName} ${user.lastName}"
                binding.userEmail.text = user.email
                topBarManager.updateUserGreeting(user)
                Log.d("MainActivity", "User data loaded: ${user.firstName}")
            } else {
                Log.d("MainActivity", "User data is null")
            }
        })

        // Спостереження за статистикою
        viewModel.userStatistics.observe(this, Observer { stats ->
            if (stats != null) {
                binding.learnedWordsCount.text = stats.learnedWordsCount.toString()
                binding.completedChaptersCount.text = "${stats.completedChapters}/${stats.totalChapters}"
                binding.studyDaysCount.text = stats.studyDaysStreak.toString()
            }
        })

        // Спостереження за станом завантаження
        viewModel.isLoading.observe(this, Observer { isLoading ->
            // Можна додати progress bar якщо потрібно
            // binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        })

        // Спостереження за помилками
        viewModel.error.observe(this, Observer { errorMessage ->
            if (errorMessage.isNotEmpty()) {
                Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
            }
        })

        // Спостереження за виходом з системи
        viewModel.logoutSuccess.observe(this, Observer { loggedOut ->
            if (loggedOut) {
                navigateToAuth()
            }
        })

        // Спостереження за неавторизованим користувачем
        viewModel.userNotAuthenticated.observe(this, Observer { notAuthenticated ->
            if (notAuthenticated) {
                navigateToAuth()
            }
        })
    }

    private fun setupClickListeners() {
        // Редагувати профіль
        binding.editProfileBtn.setOnClickListener {
            // TODO: Перехід до екрану редагування профілю
            Toast.makeText(this, "Редагування профілю - TODO", Toast.LENGTH_SHORT).show()
        }

        // Улюблені слова
        binding.favoriteWordsBtn.setOnClickListener {
            // TODO: Перехід до екрану улюблених слів
            Toast.makeText(this, "Улюблені слова - TODO", Toast.LENGTH_SHORT).show()
        }

        // Детальний прогрес
        binding.progressBtn.setOnClickListener {
            // TODO: Перехід до екрану детального прогресу
            Toast.makeText(this, "Детальний прогрес - TODO", Toast.LENGTH_SHORT).show()
        }

        // Вихід з акаунту
        binding.logoutBtn.setOnClickListener {
            showLogoutConfirmationDialog()
        }

        // Клік по аватару (можна додати зміну фото)
        binding.profileAvatar.setOnClickListener {
            // TODO: Зміна фото профілю
            Toast.makeText(this, "Зміна фото - TODO", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showLogoutConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Вийти з акаунту")
            .setMessage("Ви впевнені, що хочете вийти з акаунту?")
            .setPositiveButton("Так") { _, _ ->
                viewModel.logout()
            }
            .setNegativeButton("Скасувати", null)
            .show()
    }

    private fun navigateToAuth() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        // Повернення на головний екран
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}