package com.hedvig.app.feature.marketpicker

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.google.android.material.transition.MaterialContainerTransform
import com.google.android.material.transition.MaterialSharedAxis
import com.hedvig.app.R
import com.hedvig.app.databinding.FragmentMarketPickerBinding
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.view.updateMargin
import com.hedvig.app.util.extensions.viewBinding
import dev.chrisbanes.insetter.doOnApplyWindowInsets

class MarketPickerFragment : Fragment(R.layout.fragment_market_picker) {

    private val binding by viewBinding(FragmentMarketPickerBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val marketSelectedFragment = MarketSelectedFragment()
        marketSelectedFragment.sharedElementEnterTransition = MaterialContainerTransform()

        binding.apply {

            continueButton.doOnApplyWindowInsets { view, insets, initialState ->
                view.updateMargin(bottom = initialState.paddings.bottom + insets.systemWindowInsetBottom)
            }

            continueButton.setHapticClickListener {
                parentFragmentManager.beginTransaction()
                    .addSharedElement(continueButton, "marketButton")
                    .replace(R.id.container, marketSelectedFragment)
                    .addToBackStack(null)
                    .commit()
            }
        }
    }
}
