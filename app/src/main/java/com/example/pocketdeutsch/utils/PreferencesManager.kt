package com.example.pocketdeutsch.utils

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager(context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("PocketDeutschPrefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_WORD_OF_DAY_ID = "word_of_day_id"
        private const val KEY_WORD_OF_DAY_DATE = "word_of_day_date"
        private const val KEY_LAST_WORD_GERMAN = "last_word_german"
        private const val KEY_LAST_WORD_TRANSLATION = "last_word_translation"
        private const val KEY_USER_LEARNING_STREAK = "user_learning_streak"
        private const val KEY_LAST_LOGIN_DATE = "last_login_date"
    }

    fun saveWordOfTheDay(wordId: String, germanWord: String, translation: String) {
        val today = DateUtils.getCurrentDate()
        sharedPreferences.edit().apply {
            putString(KEY_WORD_OF_DAY_ID, wordId)
            putString(KEY_WORD_OF_DAY_DATE, today)
            putString(KEY_LAST_WORD_GERMAN, germanWord)
            putString(KEY_LAST_WORD_TRANSLATION, translation)
            apply()
        }
    }

    fun getTodaysWordOfDay(): Triple<String?, String?, String?> {
        val today = DateUtils.getCurrentDate()
        val savedDate = sharedPreferences.getString(KEY_WORD_OF_DAY_DATE, "")

        return if (savedDate == today) {
            Triple(
                sharedPreferences.getString(KEY_WORD_OF_DAY_ID, null),
                sharedPreferences.getString(KEY_LAST_WORD_GERMAN, null),
                sharedPreferences.getString(KEY_LAST_WORD_TRANSLATION, null)
            )
        } else {
            Triple(null, null, null)
        }
    }

    fun isNewDay(): Boolean {
        val today = DateUtils.getCurrentDate()
        val savedDate = sharedPreferences.getString(KEY_WORD_OF_DAY_DATE, "")
        return savedDate != today
    }

    fun updateLoginStreak() {
        val today = DateUtils.getCurrentDate()
        val lastLoginDate = sharedPreferences.getString(KEY_LAST_LOGIN_DATE, "")
        val currentStreak = sharedPreferences.getInt(KEY_USER_LEARNING_STREAK, 0)

        val newStreak = when {
            lastLoginDate.isNullOrEmpty() -> 1 // Перший вхід
            DateUtils.isToday(lastLoginDate) -> currentStreak // Сьогодні вже заходив
            DateUtils.getDaysDifference(lastLoginDate, today) == 1L -> currentStreak + 1 // Вчора заходив
            else -> 1 // Перерва в навчанні
        }

        sharedPreferences.edit().apply {
            putString(KEY_LAST_LOGIN_DATE, today)
            putInt(KEY_USER_LEARNING_STREAK, newStreak)
            apply()
        }
    }

    fun getLearningStreak(): Int {
        return sharedPreferences.getInt(KEY_USER_LEARNING_STREAK, 0)
    }

    fun clearWordOfDay() {
        sharedPreferences.edit().apply {
            remove(KEY_WORD_OF_DAY_ID)
            remove(KEY_WORD_OF_DAY_DATE)
            remove(KEY_LAST_WORD_GERMAN)
            remove(KEY_LAST_WORD_TRANSLATION)
            apply()
        }
    }

    fun clearAllData() {
        sharedPreferences.edit().clear().apply()
    }
}