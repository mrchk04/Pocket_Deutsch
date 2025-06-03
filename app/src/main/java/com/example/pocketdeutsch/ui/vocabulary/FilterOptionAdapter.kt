package com.example.pocketdeutsch.ui.vocabulary

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.pocketdeutsch.R
import com.example.pocketdeutsch.databinding.ItemFilterOptionBinding

data class FilterOption(
    val id: String,
    val title: String,
    val description: String = "",
    val iconRes: Int? = null,
    var isSelected: Boolean = false
)

class FilterOptionAdapter(
    private val options: List<FilterOption>,
    private val onOptionSelected: (FilterOption) -> Unit
) : RecyclerView.Adapter<FilterOptionAdapter.FilterOptionViewHolder>() {

    private var selectedPosition = -1

    class FilterOptionViewHolder(
        private val binding: ItemFilterOptionBinding,
        private val onOptionSelected: (FilterOption) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(option: FilterOption, isSelected: Boolean) {
            binding.apply {
                optionTitle.text = option.title

//                // Показуємо опис тільки якщо він є
//                if (option.description.isNotEmpty()) {
//                    optionDescription.text = option.description
//                    optionDescription.visibility = View.VISIBLE
//                } else {
//                    optionDescription.visibility = View.GONE
//                }
//
//                // Показуємо іконку тільки якщо вона є
//                option.iconRes?.let { iconRes ->
//                    optionIcon.setImageResource(iconRes)
//                    optionIcon.visibility = View.VISIBLE
//                } ?: run {
//                    optionIcon.visibility = View.GONE
//                }

                // Показуємо галочку для вибраної опції
                checkIcon.visibility = if (isSelected) View.VISIBLE else View.GONE

                // Встановлюємо стан вибору
                root.isSelected = isSelected

                // Обробка кліку
                root.setOnClickListener {
                    onOptionSelected(option)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FilterOptionViewHolder {
        val binding = ItemFilterOptionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return FilterOptionViewHolder(binding) { option ->
            val oldPosition = selectedPosition
            selectedPosition = options.indexOf(option)

            // Оновлюємо UI
            if (oldPosition != -1) {
                notifyItemChanged(oldPosition)
            }
            notifyItemChanged(selectedPosition)

            onOptionSelected(option)
        }
    }

    override fun onBindViewHolder(holder: FilterOptionViewHolder, position: Int) {
        holder.bind(options[position], position == selectedPosition)
    }

    override fun getItemCount(): Int = options.size

    fun clearSelection() {
        val oldPosition = selectedPosition
        selectedPosition = -1
        if (oldPosition != -1) {
            notifyItemChanged(oldPosition)
        }
    }
}