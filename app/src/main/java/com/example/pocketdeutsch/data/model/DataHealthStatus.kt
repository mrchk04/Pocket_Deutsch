package com.example.pocketdeutsch.data.model

data class DataHealthStatus(
    val wordsCount: Int,
    val topicsCount: Int,
    val chaptersCount: Int,
    val textbooksCount: Int,
    val isHealthy: Boolean,
    val error: String? = null
)