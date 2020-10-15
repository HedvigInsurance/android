package com.hedvig.app.feature.marketpicker

import android.annotation.SuppressLint
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.hedvig.app.R
import com.hedvig.app.databinding.PickerButtonBinding
import com.hedvig.app.databinding.PickerLayoutBinding
import com.hedvig.app.feature.marketing.ui.MarketingViewModel
import com.hedvig.app.feature.marketpicker.Market.NO
import com.hedvig.app.feature.marketpicker.Market.SE
import com.hedvig.app.util.GenericDiffUtilCallback
import com.hedvig.app.util.extensions.compatDrawable
import com.hedvig.app.util.extensions.getStoredBoolean
import com.hedvig.app.util.extensions.inflate
import com.hedvig.app.util.extensions.storeBoolean
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.viewBinding
import e

class PickerAdapter(
    private val parentFragmentManager: FragmentManager,
    private val viewModel: MarketPickerViewModel,
    private val marketingViewModel: MarketingViewModel,
    private val tracker: MarketPickerTracker
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
        MARKET -> ViewHolder.Market(parent)
        LANGUAGE -> ViewHolder.Language(parent)
        BUTTON -> ViewHolder.Button(parent)
        else -> throw Error("Invalid view type")
    }

    override fun getItemViewType(position: Int) = when (items[position]) {
        is Model.MarketModel -> MARKET
        is Model.LanguageModel -> LANGUAGE
        Model.Button -> BUTTON
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position], parentFragmentManager, viewModel, marketingViewModel, tracker)
    }

    override fun getItemCount() = items.size

    sealed class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        abstract fun bind(
            item: Model,
            parentFragmentManager: FragmentManager,
            viewModel: MarketPickerViewModel,
            marketingViewModel: MarketingViewModel,
            tracker: MarketPickerTracker
        )

        fun invalid(data: Model) {
            e { "Invalid data passed to ${this.javaClass.name}::bind - type is ${data.javaClass.name}" }
        }

        class Market(parent: ViewGroup) : ViewHolder(parent.inflate(R.layout.picker_layout)) {
            val binding by viewBinding(PickerLayoutBinding::bind)
            override fun bind(
                item: Model,
                parentFragmentManager: FragmentManager,
                viewModel: MarketPickerViewModel,
                marketingViewModel: MarketingViewModel,
                tracker: MarketPickerTracker
            ) {
                if (item !is Model.MarketModel || item.selection == null) {
                    return invalid(item)
                }
                binding.apply {
                    root.setHapticClickListener {
                        MarketPickerBottomSheet()
                            .show(parentFragmentManager, MarketPickerBottomSheet.TAG)
                    }
                    flag.setImageDrawable(
                        flag.context.compatDrawable(
                            when (item.selection) {
                                SE -> R.drawable.ic_flag_se
                                NO -> R.drawable.ic_flag_no
                            }
                        )
                    )
                    header.text =
                        header.context.getString(R.string.market_language_screen_market_label)
                    selected.text = selected.context.getString(
                        when (item.selection) {
                            SE -> R.string.sweden
                            NO -> R.string.norway
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
                viewModel: MarketPickerViewModel,
                marketingViewModel: MarketingViewModel,
                tracker: MarketPickerTracker
            ) {
                if (item !is Model.LanguageModel || item.selection == null) {
                    return invalid(item)
                }
                binding.apply {
                    root.setHapticClickListener {
                        LanguagePickerBottomSheet().show(
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

        class Button(parent: ViewGroup) : ViewHolder(parent.inflate(R.layout.picker_button)) {
            val binding by viewBinding(PickerButtonBinding::bind)

            @SuppressLint("ApplySharedPref") // We need to apply this now
            override fun bind(
                item: Model, parentFragmentManager: FragmentManager,
                viewModel: MarketPickerViewModel,
                marketingViewModel: MarketingViewModel,
                tracker: MarketPickerTracker
            ) {
                binding.apply {
                    val shouldProceed =
                        continueButton.context.getStoredBoolean(MarketPickerFragment.SHOULD_PROCEED)
                    continueButton.setHapticClickListener {
                        continueButton.context.storeBoolean(
                            MarketPickerFragment.SHOULD_PROCEED,
                            true
                        )
                        tracker.submit()
                        viewModel.uploadLanguage()
                        viewModel.save()
                    }
                    if (shouldProceed) {
                        marketingViewModel.navigateTo(
                            CurrentFragment.MARKETING,
                            continueButton to "marketButton"
                        )
                        continueButton.context.storeBoolean(
                            MarketPickerFragment.SHOULD_PROCEED,
                            false
                        )
                    }
                }
            }
        }
    }

    companion object {
        const val MARKET = 0
        const val LANGUAGE = 1
        const val BUTTON = 2
    }
}
