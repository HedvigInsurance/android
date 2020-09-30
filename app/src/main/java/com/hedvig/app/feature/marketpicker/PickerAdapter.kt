package com.hedvig.app.feature.marketpicker

import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.transition.MaterialContainerTransform
import com.hedvig.app.R
import com.hedvig.app.databinding.PickerButtonBinding
import com.hedvig.app.databinding.PickerLayoutBinding
import com.hedvig.app.util.GenericDiffUtilCallback
import com.hedvig.app.util.extensions.compatDrawable
import com.hedvig.app.util.extensions.inflate
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.viewBinding
import e

class PickerAdapter(
    private val parentFragmentManager: FragmentManager,
    private val viewModel: MarketPickerViewModel
) :
    RecyclerView.Adapter<PickerAdapter.ViewHolder>() {

    var items: List<Model> = emptyList()
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
        TITLE -> ViewHolder.Title(parent)
        MARKET -> ViewHolder.Market(parent)
        LANGUAGE -> ViewHolder.Language(parent)
        BUTTON -> ViewHolder.Button(parent)
        else -> throw Error("Invalid view type")
    }

    override fun getItemViewType(position: Int) = when (items[position]) {
        Model.Title -> TITLE
        is Model.MarketModel -> MARKET
        is Model.LanguageModel -> LANGUAGE
        Model.Button -> BUTTON
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position], parentFragmentManager, viewModel)
    }

    override fun getItemCount() = items.size

    sealed class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        abstract fun bind(
            item: Model,
            parentFragmentManager: FragmentManager,
            viewModel: MarketPickerViewModel
        )

        fun invalid(data: Model) {
            e { "Invalid data passed to ${this.javaClass.name}::bind - type is ${data.javaClass.name}" }
        }

        class Market(parent: ViewGroup) : ViewHolder(parent.inflate(R.layout.picker_layout)) {
            val binding by viewBinding(PickerLayoutBinding::bind)
            override fun bind(
                item: Model,
                parentFragmentManager: FragmentManager,
                viewModel: MarketPickerViewModel
            ) {
                if (item !is Model.MarketModel || item.selection == null) {
                    return invalid(item)
                }
                binding.apply {
                    val marketList = listOf(
                        com.hedvig.app.feature.marketpicker.Market.SE,
                        com.hedvig.app.feature.marketpicker.Market.NO
                    )
                    root.setHapticClickListener {
                        MarketPickerBottomSheet(marketList, viewModel)
                            .show(parentFragmentManager, MarketPickerBottomSheet.TAG)
                    }
                    flag.setImageDrawable(
                        flag.context.compatDrawable(
                            when (item.selection) {
                                com.hedvig.app.feature.marketpicker.Market.SE -> R.drawable.ic_flag_se
                                com.hedvig.app.feature.marketpicker.Market.NO -> R.drawable.ic_flag_no
                            }
                        )
                    )
                    header.text =
                        header.context.getString(R.string.market_language_screen_market_label)
                    selected.text = selected.context.getString(
                        when (item.selection) {
                            com.hedvig.app.feature.marketpicker.Market.SE -> R.string.sweden
                            com.hedvig.app.feature.marketpicker.Market.NO -> R.string.norway
                        }
                    )
                }
            }
        }

        class Language(parent: ViewGroup) : ViewHolder(parent.inflate(R.layout.picker_layout)) {
            val binding by viewBinding(PickerLayoutBinding::bind)
            override fun bind(
                item: Model,
                parentFragmentManager: FragmentManager,
                viewModel: MarketPickerViewModel
            ) {
                if (item !is Model.LanguageModel || item.selection == null) {
                    return invalid(item)
                }
                binding.apply {
                    root.setHapticClickListener {
                        LanguagePickerBottomSheet(viewModel).show(
                            parentFragmentManager,
                            LanguagePickerBottomSheet.TAG
                        )
                    }
                    flag.setImageDrawable(flag.context.compatDrawable(R.drawable.ic_language))
                    header.text =
                        header.context.getString(R.string.market_language_screen_language_label)
                    selected.text = selected.context.getString(item.selection.getLabel())
                }
            }
        }

        class Title(parent: ViewGroup) : ViewHolder(parent.inflate(R.layout.picker_title)) {
            override fun bind(
                item: Model,
                parentFragmentManager: FragmentManager,
                viewModel: MarketPickerViewModel
            ) {
            }
        }

        class Button(parent: ViewGroup) : ViewHolder(parent.inflate(R.layout.picker_button)) {
            val binding by viewBinding(PickerButtonBinding::bind)
            override fun bind(
                item: Model, parentFragmentManager: FragmentManager,
                viewModel: MarketPickerViewModel
            ) {
                val marketSelectedFragment = MarketSelectedFragment()
                marketSelectedFragment.sharedElementEnterTransition = MaterialContainerTransform()

                binding.continueButton.setHapticClickListener {
                    parentFragmentManager.beginTransaction()
                        .addSharedElement(it, "marketButton")
                        .replace(R.id.container, marketSelectedFragment)
                        .addToBackStack(null)
                        .commit()
                }
            }
        }
    }

    companion object {
        const val MARKET = 0
        const val LANGUAGE = 1
        const val BUTTON = 2
        const val TITLE = 3
    }
}
