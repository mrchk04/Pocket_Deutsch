package com.example.pocketdeutsch.ui.flashcards

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.pocketdeutsch.R
import com.example.pocketdeutsch.data.model.FlashcardWithVocabulary
import com.example.pocketdeutsch.databinding.ItemFlashcardDetailBinding
import java.text.SimpleDateFormat
import java.util.*

class FlashcardDetailAdapter(
    private val onItemClick: (FlashcardWithVocabulary) -> Unit
) : ListAdapter<FlashcardWithVocabulary, FlashcardDetailAdapter.ViewHolder>(DiffCallback()) {

    class ViewHolder(
        private val binding: ItemFlashcardDetailBinding,
        private val onItemClick: (FlashcardWithVocabulary) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        private val dateFormat = SimpleDateFormat("dd MMM", Locale.getDefault())

        fun bind(item: FlashcardWithVocabulary, position: Int) {
            binding.apply {
                // Номер позиції
                positionNumber.text = (position + 1).toString()

                // Основна інформація про слово
                germanWord.text = item.vocabularyItem.wordDe
                translation.text = item.vocabularyItem.translation

                // Налаштування видимості інформації про прогрес
                if (item.progress != null && item.progress.attempts > 0) {
                    progressInfo.visibility = View.VISIBLE

                    // Рівень впевненості (зірочки)
                    val stars = getStarsString(item.progress.confidenceLevel)
                    confidenceLevel.text = stars

                    // Кількість спроб
                    attemptsCount.text = "${item.progress.attempts} спроб"

                    // Остання дата практики
                    lastPracticed.text = when {
                        item.progress.lastPracticed == null -> "Не вивчали"
                        isToday(item.progress.lastPracticed!!) -> "Сьогодні"
                        isYesterday(item.progress.lastPracticed!!) -> "Вчора"
                        else -> dateFormat.format(item.progress.lastPracticed!!)
                    }

                    // Статус вивчення та індикатор
                    when {
                        item.progress.confidenceLevel >= 4 -> {
                            studyStatus.text = "Засвоєно"
                            confidenceIndicator.backgroundTintList =
                                ContextCompat.getColorStateList(root.context, R.color.confidence_excellent)
                        }
                        item.progress.confidenceLevel >= 3 -> {
                            studyStatus.text = "Вивчається"
                            confidenceIndicator.backgroundTintList =
                                ContextCompat.getColorStateList(root.context, R.color.confidence_medium)
                        }
                        else -> {
                            studyStatus.text = "Складне"
                            confidenceIndicator.backgroundTintList =
                                ContextCompat.getColorStateList(root.context, R.color.confidence_poor)
                        }
                    }
                } else {
                    progressInfo.visibility = View.GONE
                    studyStatus.text = "Нове"
                    confidenceIndicator.backgroundTintList =
                        ContextCompat.getColorStateList(root.context, R.color.gray5)
                }

                // Обробник кліку
                root.setOnClickListener { onItemClick(item) }
            }
        }

        private fun getStarsString(confidence: Int): String {
            return buildString {
                repeat(confidence) { append("★") }
                repeat(5 - confidence) { append("☆") }
            }
        }

        private fun isToday(date: Date): Boolean {
            val calendar = Calendar.getInstance()
            val today = calendar.time
            calendar.time = date
            val targetDay = calendar.get(Calendar.DAY_OF_YEAR)
            val targetYear = calendar.get(Calendar.YEAR)

            calendar.time = today
            val todayDay = calendar.get(Calendar.DAY_OF_YEAR)
            val todayYear = calendar.get(Calendar.YEAR)

            return targetDay == todayDay && targetYear == todayYear
        }

        private fun isYesterday(date: Date): Boolean {
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_YEAR, -1)
            val yesterday = calendar.time

            calendar.time = date
            val targetDay = calendar.get(Calendar.DAY_OF_YEAR)
            val targetYear = calendar.get(Calendar.YEAR)

            calendar.time = yesterday
            val yesterdayDay = calendar.get(Calendar.DAY_OF_YEAR)
            val yesterdayYear = calendar.get(Calendar.YEAR)

            return targetDay == yesterdayDay && targetYear == yesterdayYear
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemFlashcardDetailBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), position)
    }

    class DiffCallback : DiffUtil.ItemCallback<FlashcardWithVocabulary>() {
        override fun areItemsTheSame(
            oldItem: FlashcardWithVocabulary,
            newItem: FlashcardWithVocabulary
        ): Boolean {
            return oldItem.flashcard.id == newItem.flashcard.id
        }

        override fun areContentsTheSame(
            oldItem: FlashcardWithVocabulary,
            newItem: FlashcardWithVocabulary
        ): Boolean {
            return oldItem == newItem
        }
    }
}