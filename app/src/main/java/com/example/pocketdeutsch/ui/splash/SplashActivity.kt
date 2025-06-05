package com.example.pocketdeutsch.ui.splash

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.pocketdeutsch.databinding.ActivitySplashBinding
import com.example.pocketdeutsch.ui.intro.IntroActivity
import com.example.pocketdeutsch.ui.main.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.example.pocketdeutsch.R
import com.example.pocketdeutsch.databinding.ActivityMainBinding

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private lateinit var auth: FirebaseAuth

    companion object {
        private const val SPLASH_DELAY = 2000L // 2 секунди
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()

        // Затримка для показу splash screen, потім перевірка авторизації
        Handler(Looper.getMainLooper()).postDelayed({
            checkAuthenticationAndNavigate()
        }, SPLASH_DELAY)
    }

    private fun checkAuthenticationAndNavigate() {
        val currentUser = auth.currentUser

        if (currentUser != null) {
            // Користувач авторизований, переходимо до MainActivity
            navigateToMain()
        } else {
            // Користувач не авторизований, показуємо IntroActivity
            navigateToIntro()
        }
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun navigateToIntro() {
        val intent = Intent(this, IntroActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}