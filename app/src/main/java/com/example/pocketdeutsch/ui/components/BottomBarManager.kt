package com.example.pocketdeutsch.ui.components

import android.app.Activity
import android.content.Intent
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.example.pocketdeutsch.R

class BottomBarManager(private val activity: Activity) {

    // Елементи Homepage
    private val homepageContainer: LinearLayout by lazy {
        activity.findViewById<ImageView>(R.id.ic_home).parent as LinearLayout
    }
    private val homepageIcon: ImageView by lazy {
        activity.findViewById(R.id.ic_home)
    }
    private val homepageText: TextView by lazy {
        activity.findViewById(R.id.homepage)
    }

    // Елементи Wiederholung
    private val wiederholungContainer: LinearLayout by lazy {
        activity.findViewById<ImageView>(R.id.ic_history).parent as LinearLayout
    }
    private val wiederholungIcon: ImageView by lazy {
        activity.findViewById(R.id.ic_history)
    }
    private val wiederholungText: TextView by lazy {
        activity.findViewById(R.id.repeat)
    }

    // Елементи Interessant
    private val interessantContainer: LinearLayout by lazy {
        activity.findViewById<ImageView>(R.id.ic_lightbulb).parent as LinearLayout
    }
    private val interessantIcon: ImageView by lazy {
        activity.findViewById(R.id.ic_lightbulb)
    }
    private val interessantText: TextView by lazy {
        activity.findViewById(R.id.interesting)
    }

    fun setupNavigation(
        homepageActivityClass: Class<*>,
        wiederholungActivityClass: Class<*>? = null,
        interessantActivityClass: Class<*>? = null
    ) {
        // Homepage navigation
        homepageContainer.setOnClickListener {
            navigateToActivity(homepageActivityClass)
        }

        // Wiederholung navigation (опціонально)
        wiederholungActivityClass?.let { activityClass ->
            wiederholungContainer.setOnClickListener {
                navigateToActivity(activityClass)
            }
        }

        // Interessant navigation (опціонально)
        interessantActivityClass?.let { activityClass ->
            interessantContainer.setOnClickListener {
                navigateToActivity(activityClass)
            }
        }
    }

    fun setHomepageClickListener(action: () -> Unit) {
        homepageContainer.setOnClickListener { action() }
    }

    fun setWiederholungClickListener(action: () -> Unit) {
        wiederholungContainer.setOnClickListener { action() }
    }

    fun setInteressantClickListener(action: () -> Unit) {
        interessantContainer.setOnClickListener { action() }
    }

    private fun navigateToActivity(activityClass: Class<*>) {
        // Перевіряємо, чи не знаходимося вже на цій активності
        if (activity::class.java != activityClass) {
            val intent = Intent(activity, activityClass)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            activity.startActivity(intent)
        }
    }

    // Методи для виділення активної кнопки
    fun setActiveTab(tab: BottomBarTab) {
        resetAllTabs()
        when (tab) {
            BottomBarTab.HOMEPAGE -> {
                setTabActive(homepageIcon, homepageText)
            }
            BottomBarTab.WIEDERHOLUNG -> {
                setTabActive(wiederholungIcon, wiederholungText)
            }
            BottomBarTab.INTERESSANT -> {
                setTabActive(interessantIcon, interessantText)
            }
        }
    }

    private fun resetAllTabs() {
        setTabInactive(homepageIcon, homepageText)
        setTabInactive(wiederholungIcon, wiederholungText)
        setTabInactive(interessantIcon, interessantText)
    }

    private fun setTabActive(icon: ImageView, text: TextView) {
        try {
            icon.setColorFilter(activity.getColor(R.color.black)) // Активний колір
            text.setTextColor(activity.getColor(R.color.black))
        } catch (e: Exception) {
            // Fallback на чорний колір, якщо blue не знайдено
            icon.setColorFilter(activity.getColor(android.R.color.holo_blue_dark))
            text.setTextColor(activity.getColor(android.R.color.holo_blue_dark))
        }
    }

    private fun setTabInactive(icon: ImageView, text: TextView) {
        icon.setColorFilter(activity.getColor(R.color.gray5)) // Неактивний колір
        text.setTextColor(activity.getColor(R.color.gray5))
    }
}

enum class BottomBarTab {
    HOMEPAGE,
    WIEDERHOLUNG,
    INTERESSANT
}