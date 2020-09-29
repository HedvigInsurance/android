package com.hedvig.app.feature.marketpicker

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.hedvig.app.R
import com.hedvig.app.feature.home.ui.HomeModel
import com.hedvig.app.util.GenericDiffUtilCallback
import com.hedvig.app.util.extensions.inflate
import e

class PickerAdapter : RecyclerView.Adapter<PickerAdapter.ViewHolder>() {

    var items: List<MarketAndLanguageModel> = emptyList()
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
        MARKET -> ViewHolder.Market(parent)
        LANGUAGE -> ViewHolder.Language(parent)
        else -> throw Error("Invalid view type")
    }

    override fun getItemViewType(position: Int) = when (items[position]) {
        is MarketAndLanguageModel.MarketModel -> MARKET
        is MarketAndLanguageModel.LanguageModel -> LANGUAGE
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    sealed class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        abstract fun bind(item: MarketAndLanguageModel)

        fun invalid(data: MarketAndLanguageModel) {
            e { "Invalid data passed to ${this.javaClass.name}::bind - type is ${data.javaClass.name}" }
        }

        class Market(parent: ViewGroup) : ViewHolder(parent.inflate(R.layout.picker_layout)) {
            override fun bind(item: MarketAndLanguageModel) {
                if (item !is MarketAndLanguageModel.MarketModel) {
                    return invalid(item)
                }

            }
        }

        class Language(parent: ViewGroup) : ViewHolder(parent.inflate(R.layout.picker_layout)) {
            override fun bind(item: MarketAndLanguageModel) {
            }
        }
    }

    companion object {
        const val MARKET = 0
        const val LANGUAGE = 1
    }
}
