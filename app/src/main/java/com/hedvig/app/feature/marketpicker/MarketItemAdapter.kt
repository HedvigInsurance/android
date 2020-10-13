package com.hedvig.app.feature.marketpicker

import android.app.Dialog
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.hedvig.app.R
import com.hedvig.app.databinding.PickerItemLayoutBinding
import com.hedvig.app.feature.settings.Language
import com.hedvig.app.util.GenericDiffUtilCallback
import com.hedvig.app.util.extensions.inflate
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.viewBinding

class MarketItemAdapter(
    private val viewModel: MarketPickerViewModel,
    private val tracker: MarketPickerTracker,
    private val dialog: Dialog?
) : RecyclerView.Adapter<MarketItemAdapter.ViewHolder>() {

    var items: List<Market> = emptyList()
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(parent)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position], viewModel, tracker, dialog)
    }

    override fun getItemCount() = items.size

    class ViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
        parent.inflate(R.layout.picker_item_layout)
    ) {
        val binding by viewBinding(PickerItemLayoutBinding::bind)
        fun bind(
            market: Market,
            viewModel: MarketPickerViewModel,
            tracker: MarketPickerTracker,
            dialog: Dialog?
        ) {
            binding.apply {

                radioButton.isChecked = viewModel.data.value?.market == market
                text.text = when (market) {
                    Market.SE -> binding.text.context.getString(R.string.sweden)
                    Market.NO -> binding.text.context.getString(R.string.norway)
                }
                root.setHapticClickListener {
                    tracker.selectMarket(market)
                    viewModel.updatePickerState(
                        PickerState(
                            market,
                            Language.getAvailableLanguages(market).first()
                        )
                    )
                    viewModel.updatePickerState(viewModel.data.value?.copy(market = market))
                    viewModel.save()
                    dialog?.cancel()
                }
            }
        }
    }
}
