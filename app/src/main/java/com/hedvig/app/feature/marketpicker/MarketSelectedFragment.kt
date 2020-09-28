package com.hedvig.app.feature.marketpicker

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.google.android.material.transition.MaterialSharedAxis
import com.hedvig.app.R
import com.hedvig.app.authenticate.AuthenticateDialog
import com.hedvig.app.databinding.FragmentMarketSelectedBinding
import com.hedvig.app.feature.chat.ui.ChatActivity
import com.hedvig.app.feature.marketing.service.MarketingTracker
import com.hedvig.app.feature.norway.NorwegianAuthenticationActivity
import com.hedvig.app.feature.settings.SettingsActivity
import com.hedvig.app.feature.webonboarding.WebOnboardingActivity
import com.hedvig.app.util.extensions.getMarket
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.view.updateMargin
import com.hedvig.app.util.extensions.viewBinding
import dev.chrisbanes.insetter.doOnApplyWindowInsets
import org.koin.android.ext.android.inject

class MarketSelectedFragment : Fragment(R.layout.fragment_market_selected) {

    private val binding by viewBinding(FragmentMarketSelectedBinding::bind)
    private val tracker: MarketingTracker by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        binding.apply {
            legal.doOnApplyWindowInsets { view, insets, initialState ->
                view.updateMargin(bottom = initialState.margins.bottom + insets.systemWindowInsetBottom)
            }

            settings.doOnApplyWindowInsets { view, insets, initialState ->
                view.updateMargin(top = initialState.margins.top + insets.systemWindowInsetTop)
            }

            val market = requireContext().getMarket()
            if (market == null) {
                startActivity(MarketPickerActivity.newInstance(requireContext()))
                return
            }

            settings.setHapticClickListener {
                startActivity(SettingsActivity.newInstance(requireContext()))
            }

            signUp.setHapticClickListener {
                tracker.signUp()
                when (market) {
                    Market.SE -> startActivity(
                        ChatActivity.newInstance(requireContext())
                            .apply { putExtra(ChatActivity.EXTRA_SHOW_RESTART, true) })
                    Market.NO -> {
                        startActivity(WebOnboardingActivity.newInstance(requireContext()))
                    }
                }
            }

            logIn.setHapticClickListener {
                tracker.logIn()
                when (market) {
                    Market.SE -> {
                        AuthenticateDialog().show(parentFragmentManager, AuthenticateDialog.TAG)
                    }
                    Market.NO -> {
                        startActivity(NorwegianAuthenticationActivity.newInstance(requireContext()))
                    }
                }
            }
        }
    }
}
