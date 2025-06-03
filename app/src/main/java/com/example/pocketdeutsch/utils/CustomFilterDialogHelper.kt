package com.example.pocketdeutsch.ui.vocabulary.utils

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pocketdeutsch.R
import com.example.pocketdeutsch.data.repository.VocabularyRepository
import com.example.pocketdeutsch.databinding.DialogFilterSelectionBinding
import com.example.pocketdeutsch.ui.vocabulary.FilterOption
import com.example.pocketdeutsch.ui.vocabulary.FilterOptionAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class CustomFilterDialogHelper(
    private val context: Context,
    private val coroutineScope: CoroutineScope
) {

    fun showTopicFilterDialog(
        onTopicSelected: (String?) -> Unit,
        onFilterCleared: () -> Unit
    ) {
        coroutineScope.launch {
            try {
                val repository = VocabularyRepository(context)
                val topics = repository.getAllTopics()

                val options = topics.map { topic ->
                    FilterOption(
                        id = topic.id,
                        title = topic.title,
                        description = topic.description,
                        iconRes = R.drawable.ic_book_open
                    )
                }

                showFilterDialog(
                    title = "Виберіть тему",
                    options = options,
                    onOptionSelected = { option -> onTopicSelected(option.id) },
                    onFilterCleared = onFilterCleared
                )

            } catch (e: Exception) {
                showBasicTopicDialog(onTopicSelected, onFilterCleared)
            }
        }
    }

    private fun showBasicTopicDialog(
        onTopicSelected: (String?) -> Unit,
        onFilterCleared: () -> Unit
    ) {
        val topics = listOf(
            FilterOption("1", "Привітання та знайомство", "Begrüßung und Vorstellung", R.drawable.ic_users),
            FilterOption("2", "Особисті дані", "Personalien", R.drawable.ic_users),
            FilterOption("3", "Професії", "Berufe", R.drawable.ic_bag),
            FilterOption("4", "Офісне обладнання", "Büroausstattung", R.drawable.ic_bag),
            FilterOption("5", "Транспортні засоби", "Verkehrsmittel", R.drawable.ic_car),
            FilterOption("6", "Орієнтування в місті", "Stadtorientierung", R.drawable.ic_map),
            FilterOption("7", "Продукти харчування", "Lebensmittel", R.drawable.ic_utensils),
            FilterOption("8", "В ресторані", "Im Restaurant", R.drawable.ic_utensils)
        )

        showFilterDialog(
            title = "Виберіть тему",
            options = topics,
            onOptionSelected = { option -> onTopicSelected(option.id) },
            onFilterCleared = onFilterCleared
        )
    }

    fun showPartOfSpeechFilterDialog(
        onPartOfSpeechSelected: (String?) -> Unit,
        onFilterCleared: () -> Unit
    ) {
        val partsOfSpeech = listOf(
            FilterOption("noun", "Іменник", "noun"),
            FilterOption("verb", "Дієслово", "verb"),
            FilterOption("adjective", "Прикметник", "adjective"),
            FilterOption("adverb", "Прислівник", "adverb"),
            FilterOption("preposition", "Прийменник", "preposition"),
            FilterOption("pronoun", "Займенник", "pronoun"),
            FilterOption("article", "Артикль", "article"),
            FilterOption("conjunction", "Сполучник", "conjunction"),
            FilterOption("interjection", "Вигук", "interjection"),
            FilterOption("numeral", "Числівник", "numeral")
        )

        showFilterDialog(
            title = "Виберіть частину мови",
            options = partsOfSpeech,
            onOptionSelected = { option -> onPartOfSpeechSelected(option.id) },
            onFilterCleared = onFilterCleared
        )
    }

    fun showChapterFilterDialog(
        onChapterSelected: (String?) -> Unit,
        onFilterCleared: () -> Unit
    ) {
        val chapters = listOf(
            FilterOption("1", "Kapitel 1", "Guten Tag", R.drawable.ic_users),
            FilterOption("2", "Kapitel 2", "Erste Kontakte am Arbeitsplatz", R.drawable.ic_bag),
            FilterOption("3", "Kapitel 3", "Unterwegs in München", R.drawable.ic_car),
            FilterOption("4", "Kapitel 4", "Essen und Trinken", R.drawable.ic_utensils),
            FilterOption("5", "Kapitel 5", "Alltag", R.drawable.ic_calender),
            FilterOption("6", "Kapitel 6", "Reisen", R.drawable.ic_map),
            FilterOption("7", "Kapitel 7", "Wohnen", R.drawable.ic_home1),
            FilterOption("8", "Kapitel 8", "Begegnungen und Ereignisse", R.drawable.ic_meeting)
        )

        showFilterDialog(
            title = "Виберіть розділ",
            options = chapters,
            onOptionSelected = { option -> onChapterSelected(option.id) },
            onFilterCleared = onFilterCleared
        )
    }

    private fun showFilterDialog(
        title: String,
        options: List<FilterOption>,
        onOptionSelected: (FilterOption) -> Unit,
        onFilterCleared: () -> Unit
    ) {
        val dialog = Dialog(context)
        val binding = DialogFilterSelectionBinding.inflate(LayoutInflater.from(context))

        dialog.setContentView(binding.root)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        // Налаштування діалогу
        binding.dialogTitle.text = title

        // Налаштування RecyclerView
        val adapter = FilterOptionAdapter(options) { selectedOption ->
            onOptionSelected(selectedOption)
            dialog.dismiss()
        }

        binding.optionsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            this.adapter = adapter
        }

        // Обробники кнопок
        binding.btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        binding.btnClearFilter.setOnClickListener {
            onFilterCleared()
            dialog.dismiss()
        }

        dialog.show()
    }
}