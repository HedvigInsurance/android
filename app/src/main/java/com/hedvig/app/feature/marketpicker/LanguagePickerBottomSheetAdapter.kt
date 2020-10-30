package com.hedvig.app.feature.marketpicker

import android.app.Dialog
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hedvig.app.R
import com.hedvig.app.databinding.LanguageRecyclerItemBinding
import com.hedvig.app.databinding.PickerHeaderBinding
import com.hedvig.app.feature.settings.Language
import com.hedvig.app.util.GenericDiffUtilItemCallback
import com.hedvig.app.util.extensions.inflate
import com.hedvig.app.util.extensions.viewBinding
import e

class LanguagePickerBottomSheetAdapter(
    private val viewModel: MarketPickerViewModel,
    private val tracker: MarketPickerTracker,
    private val dialog: Dialog?
) : ListAdapter<LanguageAdapterModel, LanguagePickerBottomSheetAdapter.ViewHolder>(
    GenericDiffUtilItemCallback()
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when (viewType) {
        R.layout.picker_header -> ViewHolder.Header(parent)
        R.layout.picker_description -> ViewHolder.Description(parent)
        R.layout.language_recycler_item -> ViewHolder.Item(parent)
        else -> throw Error("Invalid view type")
    }

    override fun getItemViewType(position: Int) = when (getItem(position)) {
        LanguageAdapterModel.Header -> R.layout.picker_header
        LanguageAdapterModel.Description -> R.layout.picker_description
        is LanguageAdapterModel.LanguageList -> R.layout.language_recycler_item
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), viewModel, tracker, dialog)
    }

    sealed class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        abstract fun bind(
            item: LanguageAdapterModel,
            viewModel: MarketPickerViewModel,
            tracker: MarketPickerTracker,
            dialog: Dialog?
        )

        fun invalid(data: LanguageAdapterModel) {
            e { "Invalid data passed to ${this.javaClass.name}::bind - type is ${data.javaClass.name}" }
        }

        class Item(parent: ViewGroup) :
            ViewHolder(parent.inflate(R.layout.language_recycler_item)) {
            val binding by viewBinding(LanguageRecyclerItemBinding::bind)
            override fun bind(
                item: LanguageAdapterModel,
                viewModel: MarketPickerViewModel,
                tracker: MarketPickerTracker,
                dialog: Dialog?
            ) {
                if (item !is LanguageAdapterModel.LanguageList) {
                    return invalid(item)
                }

                binding.root.adapter =
                    LanguageItemAdapter(viewModel, tracker, dialog).also {
                        it.submitList(item.languages)
                    }
            }
        }

        class Header(parent: ViewGroup) : ViewHolder(parent.inflate(R.layout.picker_header)) {
            override fun bind(
                item: LanguageAdapterModel,
                viewModel: MarketPickerViewModel,
                tracker: MarketPickerTracker,
                dialog: Dialog?
            ) {
                val binding by viewBinding(PickerHeaderBinding::bind)
                binding.header.text =
                    binding.header.context.getText(R.string.language_picker_modal_title)
            }
        }

        class Description(parent: ViewGroup) :
            ViewHolder(parent.inflate(R.layout.picker_description)) {
            override fun bind(
                item: LanguageAdapterModel,
                viewModel: MarketPickerViewModel,
                tracker: MarketPickerTracker,
                dialog: Dialog?
            ) {
            }
        }
    }
}

sealed class LanguageAdapterModel {
    object Header : LanguageAdapterModel()
    object Description : LanguageAdapterModel()
    data class LanguageList(val languages: List<Language>) : LanguageAdapterModel()
}
