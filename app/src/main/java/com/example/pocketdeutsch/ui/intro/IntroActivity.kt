package com.example.pocketdeutsch.ui.intro

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.pocketdeutsch.databinding.ActivityIntroBinding
import com.example.pocketdeutsch.ui.auth.LoginActivity
import com.example.pocketdeutsch.ui.main.MainActivity


class IntroActivity : AppCompatActivity() {

    private lateinit var binding: ActivityIntroBinding
    private lateinit var viewModel: IntroViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIntroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Ініціалізація ViewModel
        val factory = IntroViewModelFactory(application)
        viewModel = ViewModelProvider(this, factory)[IntroViewModel::class.java]

        setupObservers()
        setupClickListeners()

        // Автоматична перевірка авторизації при запуску
        viewModel.checkAuthenticationStatus()
    }

    private fun setupObservers() {
        viewModel.isLoading.observe(this, Observer { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.getStartedBtn.isEnabled = !isLoading
        })

        viewModel.error.observe(this, Observer { errorMessage ->
            if (errorMessage.isNotEmpty()) {
                Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
            }
        })

        viewModel.navigateToMain.observe(this, Observer { shouldNavigate ->
            if (shouldNavigate) {
                navigateToMainActivity()
            }
        })

        viewModel.navigateToLogin.observe(this, Observer { shouldNavigate ->
            if (shouldNavigate) {
                navigateToLoginActivity()
            }
        })
    }

    private fun setupClickListeners() {
        binding.getStartedBtn.setOnClickListener {
            viewModel.onGetStartedClicked()
        }

//        binding.alreadyHaveAccount.setOnClickListener {
//            navigateToLoginActivity()
//        }
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun navigateToLoginActivity() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        // Не викликаємо finish(), щоб користувач міг повернутися до splash screen
    }

    override fun onResume() {
        super.onResume()
        // Перевіряємо авторизацію щоразу, коли активність стає активною
        // (наприклад, коли користувач повертається з екрану логіну)
        viewModel.checkAuthenticationStatus()
    }
}