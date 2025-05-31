package com.example.pocketdeutsch.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.pocketdeutsch.ui.main.MainActivity
import com.example.pocketdeutsch.databinding.ActivityRegisterBinding
import com.example.pocketdeutsch.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var googleSignInClient: GoogleSignInClient

    companion object {
        private const val RC_SIGN_IN = 9001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnRegister.setOnClickListener {
            registerUser()
        }

        binding.tvLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

         binding.btnGoogleSignin.setOnClickListener {
             signInWithGoogle()
         }
    }

    private fun registerUser() {
        val firstName = binding.etFirstName.text.toString().trim()
        val lastName = binding.etLastName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val confirmPassword = binding.etConfirmPassword.text.toString().trim()
        val isTermsAccepted = binding.cbTerms.isChecked

        if (validateInput(firstName, lastName, email, password, confirmPassword, isTermsAccepted)) {
            binding.btnRegister.isEnabled = false
            binding.btnRegister.text = "Реєстрація..."

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    binding.btnRegister.isEnabled = true
                    binding.btnRegister.text = "Зареєструватися"

                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        user?.let {
                            // Update user profile
                            val profileUpdates = UserProfileChangeRequest.Builder()
                                .setDisplayName("$firstName $lastName")
                                .build()

                            it.updateProfile(profileUpdates).addOnCompleteListener { profileTask ->
                                if (profileTask.isSuccessful) {
                                    // Save additional user data to Firestore
                                    saveUserDataToFirestore(it.uid, firstName, lastName, email)
                                }
                            }
                        }

                        Toast.makeText(this, "Реєстрація успішна!", Toast.LENGTH_SHORT).show()
                        startMainActivity()
                    } else {
                        val errorMessage = when (task.exception?.message) {
                            "The email address is already in use by another account." ->
                                "Цей email вже використовується"
                            "The email address is badly formatted." ->
                                "Неправильний формат email"
                            "The given password is invalid." ->
                                "Пароль повинен містити мінімум 6 символів"
                            else -> "Помилка реєстрації: ${task.exception?.message}"
                        }
                        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                    }
                }
        }
    }

    private fun validateInput(
        firstName: String,
        lastName: String,
        email: String,
        password: String,
        confirmPassword: String,
        isTermsAccepted: Boolean
    ): Boolean {

        if (firstName.isEmpty()) {
            binding.etFirstName.error = "Введіть ім'я"
            binding.etFirstName.requestFocus()
            return false
        }

        if (lastName.isEmpty()) {
            binding.etLastName.error = "Введіть прізвище"
            binding.etLastName.requestFocus()
            return false
        }

        if (email.isEmpty()) {
            binding.etEmail.error = "Введіть email"
            binding.etEmail.requestFocus()
            return false
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmail.error = "Введіть правильний email"
            binding.etEmail.requestFocus()
            return false
        }

        if (password.isEmpty()) {
            binding.etPassword.error = "Введіть пароль"
            binding.etPassword.requestFocus()
            return false
        }

        if (password.length < 6) {
            binding.etPassword.error = "Пароль повинен містити мінімум 6 символів"
            binding.etPassword.requestFocus()
            return false
        }

        if (confirmPassword.isEmpty()) {
            binding.etConfirmPassword.error = "Підтвердіть пароль"
            binding.etConfirmPassword.requestFocus()
            return false
        }

        if (password != confirmPassword) {
            binding.etConfirmPassword.error = "Паролі не співпадають"
            binding.etConfirmPassword.requestFocus()
            return false
        }

        if (!isTermsAccepted) {
            Toast.makeText(this, "Прийміть умови використання", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun saveUserDataToFirestore(uid: String, firstName: String, lastName: String, email: String) {
        val userData = hashMapOf(
            "firstName" to firstName,
            "lastName" to lastName,
            "email" to email,
            "createdAt" to System.currentTimeMillis(),
            "level" to 1,
            "experience" to 0
        )

        firestore.collection("users").document(uid)
            .set(userData)
            .addOnSuccessListener {
                // User data saved successfully
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Помилка збереження даних: ${e.message}",
                    Toast.LENGTH_SHORT).show()
            }
    }

    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Toast.makeText(this, "Google реєстрація не вдалася: ${e.message}",
                    Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    user?.let {
                        // Save Google user data to Firestore
                        val names = it.displayName?.split(" ") ?: listOf("", "")
                        val firstName = names.getOrNull(0) ?: ""
                        val lastName = names.getOrNull(1) ?: ""

                        saveUserDataToFirestore(it.uid, firstName, lastName, it.email ?: "")
                    }

                    Toast.makeText(this, "Успішна реєстрація через Google!", Toast.LENGTH_SHORT).show()
                    startMainActivity()
                } else {
                    Toast.makeText(this, "Помилка авторизації: ${task.exception?.message}",
                        Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun startMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}