package com.hedvig.app.feature.marketpicker

import android.os.Bundle
import android.view.View
import androidx.core.view.doOnNextLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.hedvig.app.R
import com.hedvig.app.databinding.FragmentMarketPickerBinding
import com.hedvig.app.feature.marketing.ui.MarketingViewModelImpl
import com.hedvig.app.feature.marketing.ui.NavigationState
import com.hedvig.app.util.extensions.view.applyNavigationBarInsetsMargin
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import org.koin.android.ext.android.inject

@AndroidEntryPoint
class MarketPickerFragment : Fragment(R.layout.fragment_market_picker) {
    private val model: MarketPickerViewModelImpl by viewModels()
    private val marketingViewModel: MarketingViewModelImpl by viewModels()
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
            picker.applyNavigationBarInsetsMargin()

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
