package com.hedvig.app.feature.marketpicker

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.hedvig.app.R
import com.hedvig.app.databinding.FragmentMarketPickerBinding
import com.hedvig.app.feature.marketing.ui.MarketingActivity
import com.hedvig.app.feature.marketing.ui.MarketingViewModel
import com.hedvig.app.util.extensions.storeBoolean
import com.hedvig.app.util.extensions.view.updateMargin
import com.hedvig.app.util.extensions.viewBinding
import dev.chrisbanes.insetter.doOnApplyWindowInsets
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.sharedViewModel

class MarketPickerFragment : Fragment(R.layout.fragment_market_picker) {
    private val viewModel: MarketPickerViewModel by sharedViewModel()
    private val marketingViewModel: MarketingViewModel by sharedViewModel()
    private val binding by viewBinding(FragmentMarketPickerBinding::bind)
    private val tracker: MarketPickerTracker by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        requireContext().storeBoolean(MarketingActivity.SHOULD_OPEN_MARKET_SELECTED, false)

        binding.apply {
            picker.doOnApplyWindowInsets { view, insets, initialState ->
                view.updateMargin(bottom = initialState.paddings.bottom + insets.systemWindowInsetBottom)
            }

            picker.adapter =
                PickerAdapter(parentFragmentManager, viewModel, marketingViewModel, tracker)

            var firstLayout = true
            viewModel.data.observe(viewLifecycleOwner) { data ->
                (picker.adapter as PickerAdapter).apply {
                    submitList(
                        listOf(
                            Model.Button,
                            Model.LanguageModel(data.language),
                            Model.MarketModel(data.market)
                        )
                    )
                }
                // Need too do this else the recyclerview might not show until user scrolls
                if (firstLayout) {
                    firstLayout = false
                    picker.smoothScrollToPosition(0)
                }
            }
        }
    }

    companion object {
        const val SHOULD_PROCEED = "SHOULD_PROCEED"
    }
}
