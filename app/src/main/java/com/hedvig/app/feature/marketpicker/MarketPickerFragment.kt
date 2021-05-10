package com.hedvig.app.feature.marketpicker

import android.os.Bundle
import android.view.View
import androidx.core.view.doOnNextLayout
import androidx.fragment.app.Fragment
import com.hedvig.app.R
import com.hedvig.app.databinding.FragmentMarketPickerBinding
import com.hedvig.app.feature.marketing.ui.MarketingViewModel
import com.hedvig.app.feature.marketing.ui.NavigationState
import com.hedvig.app.util.extensions.view.updateMargin
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import dev.chrisbanes.insetter.doOnApplyWindowInsets
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class MarketPickerFragment : Fragment(R.layout.fragment_market_picker) {
    private val model: MarketPickerViewModel by sharedViewModel()
    private val marketingViewModel: MarketingViewModel by sharedViewModel()
    private val binding by viewBinding(FragmentMarketPickerBinding::bind)
    private val tracker: MarketPickerTracker by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        postponeEnterTransition()

        binding.apply {
            picker.adapter = PickerAdapter(
                parentFragmentManager,
                onSubmit = { sharedElements ->
                    tracker.submit()
                    model.submit()
                    marketingViewModel.navigateTo(
                        NavigationState(
                            destination = CurrentFragment.MARKETING,
                            sharedElements = sharedElements,
                            reorderingAllowed = true,
                            addToBackStack = true,
                        )
                    )
                }
            )
            picker.doOnApplyWindowInsets { view, insets, initialState ->
                view.updateMargin(bottom = initialState.paddings.bottom + insets.systemWindowInsetBottom)
            }

            var firstLayout = true
            model.pickerState.observe(viewLifecycleOwner) { data ->
                (picker.adapter as PickerAdapter).apply {
                    submitList(
                        listOf(
                            Model.Button,
                            Model.LanguageModel(data.language),
                            Model.MarketModel(data.market)
                        )
                    )
                }
                // Need to do this else the recyclerview might not show until user scrolls
                if (firstLayout) {
                    firstLayout = false
                    picker.smoothScrollToPosition(0)
                }

                picker.doOnNextLayout {
                    startPostponedEnterTransition()
                }
            }
        }
    }
}
