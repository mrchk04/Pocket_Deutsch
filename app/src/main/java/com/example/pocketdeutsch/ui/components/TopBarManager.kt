package com.example.pocketdeutsch.ui.components

import android.app.Activity
import android.widget.ImageView
import android.widget.TextView
import com.example.pocketdeutsch.R
import com.example.pocketdeutsch.data.model.User

class TopBarManager(private val activity: Activity) {

    private val greetingTextView: TextView by lazy {
        activity.findViewById(R.id.hallo_marii)
    }

    private val profileImageView: ImageView by lazy {
        activity.findViewById(R.id.imageView1)
    }

    fun updateUserGreeting(user: User?) {
        if (user != null) {
            greetingTextView.text = "Hallo, ${user.firstName}!"
        } else {
            greetingTextView.text = "Hallo"
        }
    }

    fun updateUserGreeting(firstName: String) {
        greetingTextView.text = "Hallo, $firstName!"
    }

    fun setProfileClickListener(action: () -> Unit) {
        profileImageView.setOnClickListener { action() }
    }

    fun setGreetingClickListener(action: () -> Unit) {
        greetingTextView.setOnClickListener { action() }
    }
}