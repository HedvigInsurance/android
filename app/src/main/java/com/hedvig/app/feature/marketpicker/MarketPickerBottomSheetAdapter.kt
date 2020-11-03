package com.hedvig.app.feature.marketpicker

import android.app.Dialog
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hedvig.app.R
import com.hedvig.app.databinding.LanguageRecyclerItemBinding
import com.hedvig.app.databinding.PickerHeaderBinding
import com.hedvig.app.util.GenericDiffUtilItemCallback
import com.hedvig.app.util.extensions.inflate
import com.hedvig.app.util.extensions.viewBinding
import e

class MarketPickerBottomSheetAdapter(
    private val viewModel: MarketPickerViewModel,
    private val tracker: MarketPickerTracker,
    private val dialog: Dialog?
) : ListAdapter<MarketAdapterModel, MarketPickerBottomSheetAdapter.ViewHolder>(
    GenericDiffUtilItemCallback()
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when (viewType) {
        R.layout.picker_header -> ViewHolder.Header(parent)
        R.layout.language_recycler_item -> ViewHolder.Item(parent)
        else -> throw Error("Invalid view type")
    }

    override fun getItemViewType(position: Int) = when (getItem(position)) {
        MarketAdapterModel.Header -> R.layout.picker_header
        is MarketAdapterModel.MarketList -> R.layout.language_recycler_item
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), viewModel, tracker, dialog)
    }

    sealed class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        abstract fun bind(
            item: MarketAdapterModel,
            viewModel: MarketPickerViewModel,
            tracker: MarketPickerTracker,
            dialog: Dialog?
        )

        fun invalid(data: MarketAdapterModel) {
            e { "Invalid data passed to ${this.javaClass.name}::bind - type is ${data.javaClass.name}" }
        }

        class Item(parent: ViewGroup) :
            ViewHolder(parent.inflate(R.layout.language_recycler_item)) {
            val binding by viewBinding(LanguageRecyclerItemBinding::bind)
            override fun bind(
                item: MarketAdapterModel,
                viewModel: MarketPickerViewModel,
                tracker: MarketPickerTracker,
                dialog: Dialog?
            ) {
                if (item !is MarketAdapterModel.MarketList) {
                    return invalid(item)
                }

                binding.root.adapter =
                    MarketItemAdapter(
                        viewModel,
                        tracker,
                        dialog
                    ).also { it.submitList(item.markets) }
            }
        }

        class Header(parent: ViewGroup) : ViewHolder(parent.inflate(R.layout.picker_header)) {
            override fun bind(
                item: MarketAdapterModel,
                viewModel: MarketPickerViewModel,
                tracker: MarketPickerTracker,
                dialog: Dialog?
            ) {
                val binding by viewBinding(PickerHeaderBinding::bind)
                binding.header.setText(R.string.market_picker_modal_title)
            }
        }
    }
}

sealed class MarketAdapterModel {
    object Header : MarketAdapterModel()
    data class MarketList(val markets: List<Market>) : MarketAdapterModel()
}
