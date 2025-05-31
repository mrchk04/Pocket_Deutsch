package com.example.pocketdeutsch.data.model

import com.google.firebase.firestore.PropertyName

data class User(
    @PropertyName("id")
    val id: String = "",

    @PropertyName("email")
    val email: String = "",

    @PropertyName("firstName")
    val firstName: String = "",

    @PropertyName("lastName")
    val lastName: String = "",

    val createdAt: Long = 0,
    val level: Int = 0,
    val experience: Int = 0
) {
    // Порожній конструктор для Firebase
    constructor() : this("", "", "", "", 0, 0, 0)
}