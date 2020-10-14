package com.hedvig.app.feature.marketpicker

import android.app.Dialog
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.hedvig.app.R
import com.hedvig.app.databinding.LanguageRecyclerItemBinding
import com.hedvig.app.databinding.PickerHeaderBinding
import com.hedvig.app.databinding.PickerItemLayoutBinding
import com.hedvig.app.feature.settings.Language
import com.hedvig.app.util.GenericDiffUtilCallback
import com.hedvig.app.util.extensions.inflate
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.viewBinding
import e

class MarketPickerBottomSheetAdapter(
    private val viewModel: MarketPickerViewModel,
    private val tracker: MarketPickerTracker,
    private val dialog: Dialog?
) : RecyclerView.Adapter<MarketPickerBottomSheetAdapter.ViewHolder>() {
    var items: List<MarketAdapterModel> = emptyList()
        set(value) {
            val diff = DiffUtil.calculateDiff(
                GenericDiffUtilCallback(
                    field,
                    value
                )
            )
            field = value
            diff.dispatchUpdatesTo(this)
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when (viewType) {
        R.layout.picker_header -> ViewHolder.Header(parent)
        R.layout.language_recycler_item -> ViewHolder.Item(parent)
        else -> throw Error("Invalid view type")
    }

    override fun getItemViewType(position: Int) = when (items[position]) {
        MarketAdapterModel.Header -> R.layout.picker_header
        is MarketAdapterModel.MarketList -> R.layout.language_recycler_item
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position], viewModel, tracker, dialog)
    }

    override fun getItemCount() = items.size

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
                    MarketItemAdapter(viewModel, tracker, dialog).also {
                        it.items = item.markets
                    }
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
                binding.header.text = binding.header.context.getText(R.string.market_picker_modal_title)
            }
        }
    }
}

sealed class MarketAdapterModel {
    object Header : MarketAdapterModel()
    data class MarketList(val markets: List<Market>) : MarketAdapterModel()
}
