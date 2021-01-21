package com.hedvig.app.feature.marketpicker

import android.annotation.SuppressLint
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hedvig.app.R
import com.hedvig.app.databinding.PickerButtonBinding
import com.hedvig.app.databinding.PickerLayoutBinding
import com.hedvig.app.feature.marketing.ui.MarketingViewModel
import com.hedvig.app.util.GenericDiffUtilItemCallback
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
) : ListAdapter<Model, PickerAdapter.ViewHolder>(GenericDiffUtilItemCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when (viewType) {
        MARKET -> ViewHolder.Market(parent)
        LANGUAGE -> ViewHolder.Language(parent)
        BUTTON -> ViewHolder.Button(parent)
        else -> throw Error("Invalid view type")
    }

    override fun getItemViewType(position: Int) = when (getItem(position)) {
        is Model.MarketModel -> MARKET
        is Model.LanguageModel -> LANGUAGE
        Model.Button -> BUTTON
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(
            getItem(position),
            parentFragmentManager,
            viewModel,
            marketingViewModel,
            tracker
        )
    }

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
                    flag.setImageResource(item.selection.flag)
                    header.setText(R.string.market_language_screen_market_label)
                    selected.setText(item.selection.label)
                }
            }
        }

        class Language(parent: ViewGroup) : ViewHolder(parent.inflate(R.layout.picker_layout)) {
            private val binding by viewBinding(PickerLayoutBinding::bind)
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
                    flag.setImageResource(R.drawable.ic_language)
                    header.setText(R.string.market_language_screen_language_label)
                    selected.setText(item.selection.getLabel())
                }
            }
        }

        class Button(parent: ViewGroup) : ViewHolder(parent.inflate(R.layout.picker_button)) {
            val binding by viewBinding(PickerButtonBinding::bind)

            @SuppressLint("ApplySharedPref") // We need to apply this now
            override fun bind(
                item: Model,
                parentFragmentManager: FragmentManager,
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
