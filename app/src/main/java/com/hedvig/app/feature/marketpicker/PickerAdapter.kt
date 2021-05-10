package com.hedvig.app.feature.marketpicker

import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hedvig.app.R
import com.hedvig.app.databinding.PickerButtonBinding
import com.hedvig.app.databinding.PickerLayoutBinding
import com.hedvig.app.feature.marketing.ui.MarketingActivity
import com.hedvig.app.util.GenericDiffUtilItemCallback
import com.hedvig.app.util.extensions.inflate
import com.hedvig.app.util.extensions.invalid
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.viewBinding

class PickerAdapter(
    private val parentFragmentManager: FragmentManager,
    private val onSubmit: (List<Pair<View, String>>) -> Unit,
) : ListAdapter<Model, PickerAdapter.ViewHolder>(GenericDiffUtilItemCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when (viewType) {
        MARKET -> ViewHolder.Market(parent, parentFragmentManager)
        LANGUAGE -> ViewHolder.Language(parent, parentFragmentManager)
        BUTTON -> ViewHolder.Button(parent, onSubmit)
        else -> throw Error("Invalid view type")
    }

    override fun getItemViewType(position: Int) = when (getItem(position)) {
        is Model.MarketModel -> MARKET
        is Model.LanguageModel -> LANGUAGE
        Model.Button -> BUTTON
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    sealed class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        abstract fun bind(item: Model)

        class Market(
            parent: ViewGroup,
            private val parentFragmentManager: FragmentManager,
        ) : ViewHolder(parent.inflate(R.layout.picker_layout)) {
            private val binding by viewBinding(PickerLayoutBinding::bind)

            override fun bind(item: Model) {
                if (item !is Model.MarketModel) {
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

        class Language(
            parent: ViewGroup,
            private val parentFragmentManager: FragmentManager,
        ) : ViewHolder(parent.inflate(R.layout.picker_layout)) {
            private val binding by viewBinding(PickerLayoutBinding::bind)

            override fun bind(item: Model) {
                if (item !is Model.LanguageModel) {
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

        class Button(
            parent: ViewGroup,
            private val onSubmit: (List<Pair<View, String>>) -> Unit,
        ) : ViewHolder(parent.inflate(R.layout.picker_button)) {
            private val binding by viewBinding(PickerButtonBinding::bind)

            override fun bind(item: Model) {
                binding.continueButton.setHapticClickListener {
                    onSubmit(listOf(binding.continueButton to MarketingActivity.SHARED_ELEMENT_NAME))
                }
            }
        }
    }

    companion object {
        private const val MARKET = 0
        private const val LANGUAGE = 1
        private const val BUTTON = 2
    }
}
