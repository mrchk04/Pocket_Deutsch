package com.example.pocketdeutsch.ui.flashcards

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.pocketdeutsch.R
import com.example.pocketdeutsch.data.model.FlashcardSetWithProgress
import com.example.pocketdeutsch.databinding.ItemFlashcardSetBinding
import java.text.SimpleDateFormat
import java.util.*

class FlashcardSetsAdapter(
    private val onItemClick: (FlashcardSetWithProgress) -> Unit,
    private val onStudyClick: (FlashcardSetWithProgress) -> Unit
) : ListAdapter<FlashcardSetWithProgress, FlashcardSetsAdapter.ViewHolder>(DiffCallback()) {

    class ViewHolder(
        private val binding: ItemFlashcardSetBinding,
        private val onItemClick: (FlashcardSetWithProgress) -> Unit,
        private val onStudyClick: (FlashcardSetWithProgress) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        private val dateFormat = SimpleDateFormat("dd MMM", Locale.getDefault())

        fun bind(item: FlashcardSetWithProgress) {
            binding.apply {
                // Основна інформація
                setTitle.text = item.flashcardSet.title
                topicTitle.text = item.topic?.title ?: "Загальна тема"

                // Прогрес
                val progressValue = if (item.totalCards > 0) {
                    (item.studiedCards * 100) / item.totalCards
                } else 0

                progressText.text = "${item.studiedCards} з ${item.totalCards} карток"
                progressBar.progress = progressValue
                progressPercentage.text = "$progressValue%"

                // Рівень впевненості (зірочки)
                val stars = getStarsString(item.averageConfidence)
                confidenceLevel.text = stars

                // Остання дата навчання
                lastStudied.text = when {
                    item.lastStudied == null -> "Ще не вивчали"
                    isToday(item.lastStudied) -> "Сьогодні"
                    isYesterday(item.lastStudied) -> "Вчора"
                    else -> dateFormat.format(item.lastStudied)
                }

                // Іконка залежно від теми
                topicIcon.setImageResource(getTopicIcon(item.topic?.title))

                // Обробники кліків
                root.setOnClickListener {
                    Log.d("FlashcardSetsAdapter", "Item clicked: ${item.flashcardSet.title}")
                    onItemClick(item)
                }
                btnStudy.setOnClickListener {
                    Log.d("FlashcardSetsAdapter", "Study button clicked: ${item.flashcardSet.title}")
                    onStudyClick(item)
                }

                // Зміна кольору кнопки залежно від прогресу
                if (progressValue == 0) {
                    btnStudy.setBackgroundResource(R.drawable.rounded_button1)
                    btnStudy.setTextColor(binding.root.context.getColor(R.color.gray5))
                } else {
                    btnStudy.setBackgroundResource(R.drawable.rounded_button)
                    btnStudy.setTextColor(binding.root.context.getColor(R.color.black))
                }
            }
        }

        private fun getStarsString(confidence: Float): String {
            val fullStars = confidence.toInt()
            val hasHalfStar = confidence - fullStars >= 0.5f

            return buildString {
                repeat(fullStars) { append("★") }
                if (hasHalfStar && fullStars < 5) append("☆")
                repeat(5 - fullStars - if (hasHalfStar) 1 else 0) { append("☆") }
            }
        }

        private fun getTopicIcon(topicTitle: String?): Int {
            return when (topicTitle?.lowercase()) {
                "familie" -> R.drawable.ic_users
                "essen", "restaurant" -> R.drawable.ic_utensils
                "reisen" -> R.drawable.ic_map
                "wohnen" -> R.drawable.ic_home1
                "arbeit" -> R.drawable.ic_bag
                "freizeit" -> R.drawable.ic_calender
                else -> R.drawable.ic_book_open
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
        val binding = ItemFlashcardSetBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding, onItemClick, onStudyClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<FlashcardSetWithProgress>() {
        override fun areItemsTheSame(
            oldItem: FlashcardSetWithProgress,
            newItem: FlashcardSetWithProgress
        ): Boolean {
            return oldItem.flashcardSet.id == newItem.flashcardSet.id
        }

        override fun areContentsTheSame(
            oldItem: FlashcardSetWithProgress,
            newItem: FlashcardSetWithProgress
        ): Boolean {
            return oldItem == newItem
        }
    }
}