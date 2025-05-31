package com.example.pocketdeutsch.data.repository

import com.example.pocketdeutsch.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class UserRepository {

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val usersCollection = firestore.collection("users")

    fun isUserAuthenticated(): Boolean {
        return firebaseAuth.currentUser != null
    }

    suspend fun getCurrentUser(): User? {
        try {
            val currentUser = firebaseAuth.currentUser ?: return null
            val userId = currentUser.uid

            val document = usersCollection.document(userId).get().await()

            if (document.exists()) {
                return document.toObject(User::class.java)?.copy(id = userId)
            }

            return null
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun updateUser(user: User): Boolean {
        try {
            val currentUser = firebaseAuth.currentUser ?: return false
            val userId = currentUser.uid

            usersCollection.document(userId).set(user).await()
            return true
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun createUser(user: User): Boolean {
        try {
            val currentUser = firebaseAuth.currentUser ?: return false
            val userId = currentUser.uid

            val userWithId = user.copy(id = userId)
            usersCollection.document(userId).set(userWithId).await()
            return true
        } catch (e: Exception) {
            throw e
        }
    }

    fun signOut() {
        firebaseAuth.signOut()
    }
}