package com.example.pocketdeutsch.ui.vocabulary

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.pocketdeutsch.R
import com.example.pocketdeutsch.data.model.VocabularyItem
import com.example.pocketdeutsch.databinding.ItemVocabularyWordBinding

class VocabularyAdapter(
    private val onItemClick: (VocabularyItem) -> Unit,
    private val onFavoriteClick: (VocabularyItem, Boolean) -> Unit,
) : ListAdapter<VocabularyAdapter.VocabularyItemWithFavorite, VocabularyAdapter.VocabularyViewHolder>(VocabularyDiffCallback()) {

    data class VocabularyItemWithFavorite(
        val vocabularyItem: VocabularyItem,
        val favorite: Boolean
    )

    class VocabularyViewHolder(
        private val binding: ItemVocabularyWordBinding,
        private val onItemClick: (VocabularyItem) -> Unit,
        private val onFavoriteClick: (VocabularyItem, Boolean) -> Unit,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: VocabularyItemWithFavorite) {
            val vocabularyItem = item.vocabularyItem

            // ДОДАЙТЕ ЦІ ЛОГИ ДЛЯ ВІДЛАДКИ
            Log.d("VocabularyAdapter", "🔍 Binding word: '${vocabularyItem.wordDe}'")
            Log.d("VocabularyAdapter", "🔍 Word ID: '${vocabularyItem.id}'")
            Log.d("VocabularyAdapter", "🔍 Is favorite: ${item.favorite}")

            binding.apply {
                germanWord.text = vocabularyItem.wordDe
                translation.text = vocabularyItem.translation
                partOfSpeech.text = vocabularyItem.partOfSpeech

                // Налаштування видимості та тексту додаткової інформації
                if (vocabularyItem.pluralForm.isNotEmpty() || vocabularyItem.exampleSentence.isNotEmpty()) {
                    pluralForm.text = vocabularyItem.pluralForm
                    exampleSentence.text = vocabularyItem.exampleSentence

                    pluralForm.visibility = if (vocabularyItem.pluralForm.isNotEmpty()) View.VISIBLE else View.GONE
                    exampleSentence.visibility = if (vocabularyItem.exampleSentence.isNotEmpty()) View.VISIBLE else View.GONE
                } else {
                    additionalInfoLayout.visibility = View.GONE
                }

                // Налаштування іконки улюбленого
                val favoriteIcon = if (item.favorite) {
                    R.drawable.ic_heart_filled
                } else {
                    R.drawable.ic_heart_outline
                }
                favoriteButton.setImageResource(favoriteIcon)

                // Показати/приховати кнопку аудіо залежно від наявності URL
                audioButton.visibility = if (vocabularyItem.audioUrl.isNotEmpty()) View.VISIBLE else View.GONE

                // Клік по елементу - розгортання додаткової інформації
                root.setOnClickListener {
//                    toggleAdditionalInfo()
                    onItemClick(vocabularyItem)
                }

                // Клік по кнопці улюбленого
                favoriteButton.setOnClickListener {

                    Log.d("VocabularyAdapter", "❤️ Favorite clicked for word: '${vocabularyItem.wordDe}' with ID: '${vocabularyItem.id}'")

                    onFavoriteClick(vocabularyItem, !item.favorite)
                }
            }
        }

//        private fun toggleAdditionalInfo() {
//            binding.apply {
//                if (additionalInfoLayout.visibility == View.GONE) {
//                    additionalInfoLayout.visibility = View.VISIBLE
//                } else {
//                    additionalInfoLayout.visibility = View.GONE
//                }
//            }
//        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VocabularyViewHolder {
        val binding = ItemVocabularyWordBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return VocabularyViewHolder(binding,
            onItemClick,
            onFavoriteClick)
    }

    override fun onBindViewHolder(holder: VocabularyViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class VocabularyDiffCallback : DiffUtil.ItemCallback<VocabularyItemWithFavorite>() {
        override fun areItemsTheSame(
            oldItem: VocabularyItemWithFavorite,
            newItem: VocabularyItemWithFavorite
        ): Boolean {
            return oldItem.vocabularyItem.id == newItem.vocabularyItem.id
        }

        override fun areContentsTheSame(
            oldItem: VocabularyItemWithFavorite,
            newItem: VocabularyItemWithFavorite
        ): Boolean {
            return oldItem == newItem
        }
    }
}