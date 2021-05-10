package com.hedvig.app.feature.marketpicker

import android.app.Dialog
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hedvig.app.R
import com.hedvig.app.databinding.PickerHeaderBinding
import com.hedvig.app.databinding.PickerItemLayoutBinding
import com.hedvig.app.feature.settings.Language
import com.hedvig.app.util.GenericDiffUtilItemCallback
import com.hedvig.app.util.extensions.inflate
import com.hedvig.app.util.extensions.invalid
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.viewBinding

class LanguagePickerBottomSheetAdapter(
    private val viewModel: MarketPickerViewModel,
    private val tracker: MarketPickerTracker,
    private val dialog: Dialog?,
) : ListAdapter<LanguageAdapterModel, LanguagePickerBottomSheetAdapter.ViewHolder>(
    GenericDiffUtilItemCallback()
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when (viewType) {
        R.layout.picker_header -> ViewHolder.Header(parent)
        R.layout.picker_description -> ViewHolder.Description(parent)
        R.layout.picker_item_layout -> ViewHolder.Item(parent)
        else -> throw Error("Invalid view type")
    }

    override fun getItemViewType(position: Int) = when (getItem(position)) {
        LanguageAdapterModel.Header -> R.layout.picker_header
        LanguageAdapterModel.Description -> R.layout.picker_description
        is LanguageAdapterModel.LanguageItem -> R.layout.picker_item_layout
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), viewModel, tracker, dialog)
    }

    sealed class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        abstract fun bind(
            item: LanguageAdapterModel,
            viewModel: MarketPickerViewModel,
            tracker: MarketPickerTracker,
            dialog: Dialog?,
        )

        class Item(parent: ViewGroup) :
            ViewHolder(parent.inflate(R.layout.picker_item_layout)) {
            private val binding by viewBinding(PickerItemLayoutBinding::bind)
            override fun bind(
                item: LanguageAdapterModel,
                viewModel: MarketPickerViewModel,
                tracker: MarketPickerTracker,
                dialog: Dialog?,
            ) {
                if (item !is LanguageAdapterModel.LanguageItem) {
                    return invalid(item)
                }

                binding.apply {
                    radioButton.isChecked = viewModel.pickerState.value?.language == item.inner
                    text.text = text.context.getString(item.inner.getLabel())

                    root.setHapticClickListener {
                        tracker.selectLocale(item.inner)
                        viewModel.applyLanguageAndReload(item.inner)
                        dialog?.cancel()
                    }
                }
            }
        }

        class Header(parent: ViewGroup) : ViewHolder(parent.inflate(R.layout.picker_header)) {
            private val binding by viewBinding(PickerHeaderBinding::bind)
            override fun bind(
                item: LanguageAdapterModel,
                viewModel: MarketPickerViewModel,
                tracker: MarketPickerTracker,
                dialog: Dialog?,
            ) {
                binding.header.setText(R.string.language_picker_modal_title)
            }
        }

        class Description(parent: ViewGroup) :
            ViewHolder(parent.inflate(R.layout.picker_description)) {
            override fun bind(
                item: LanguageAdapterModel,
                viewModel: MarketPickerViewModel,
                tracker: MarketPickerTracker,
                dialog: Dialog?,
            ) {
            }
        }
    }
}

sealed class LanguageAdapterModel {
    object Header : LanguageAdapterModel()
    object Description : LanguageAdapterModel()
    data class LanguageItem(val inner: Language) : LanguageAdapterModel()
}
