package com.hedvig.app.feature.marketpicker

import android.app.Dialog
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hedvig.app.R
import com.hedvig.app.databinding.PickerHeaderBinding
import com.hedvig.app.databinding.PickerItemLayoutBinding
import com.hedvig.app.feature.settings.Market
import com.hedvig.app.util.GenericDiffUtilItemCallback
import com.hedvig.app.util.extensions.inflate
import com.hedvig.app.util.extensions.invalid
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.viewBinding

class MarketPickerBottomSheetAdapter(
    private val viewModel: MarketPickerViewModel,
    private val tracker: MarketPickerTracker,
    private val dialog: Dialog?,
) : ListAdapter<MarketAdapterModel, MarketPickerBottomSheetAdapter.ViewHolder>(
    GenericDiffUtilItemCallback()
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when (viewType) {
        R.layout.picker_header -> ViewHolder.Header(parent)
        R.layout.picker_item_layout -> ViewHolder.Item(parent)
        else -> throw Error("Invalid view type")
    }

    override fun getItemViewType(position: Int) = when (getItem(position)) {
        MarketAdapterModel.Header -> R.layout.picker_header
        is MarketAdapterModel.MarketItem -> R.layout.picker_item_layout
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), viewModel, tracker, dialog)
    }

    sealed class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        abstract fun bind(
            item: MarketAdapterModel,
            viewModel: MarketPickerViewModel,
            tracker: MarketPickerTracker,
            dialog: Dialog?,
        )

        class Item(parent: ViewGroup) :
            ViewHolder(parent.inflate(R.layout.picker_item_layout)) {
            private val binding by viewBinding(PickerItemLayoutBinding::bind)
            override fun bind(
                item: MarketAdapterModel,
                viewModel: MarketPickerViewModel,
                tracker: MarketPickerTracker,
                dialog: Dialog?,
            ) {
                if (item !is MarketAdapterModel.MarketItem) {
                    return invalid(item)
                }

                binding.apply {
                    radioButton.isChecked = viewModel.pickerState.value?.market == item.inner
                    text.setText(item.inner.label)
                    root.setHapticClickListener {
                        tracker.selectMarket(item.inner)
                        viewModel.applyMarketAndReload(item.inner)
                        dialog?.cancel()
                    }
                }
            }
        }

        class Header(parent: ViewGroup) : ViewHolder(parent.inflate(R.layout.picker_header)) {
            private val binding by viewBinding(PickerHeaderBinding::bind)
            override fun bind(
                item: MarketAdapterModel,
                viewModel: MarketPickerViewModel,
                tracker: MarketPickerTracker,
                dialog: Dialog?,
            ) {
                binding.header.setText(R.string.market_picker_modal_title)
            }
        }
    }
}

sealed class MarketAdapterModel {
    object Header : MarketAdapterModel()
    data class MarketItem(val inner: Market) : MarketAdapterModel()
}
