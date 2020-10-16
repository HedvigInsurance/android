package com.hedvig.app.feature.marketpicker

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.hedvig.app.R
import com.hedvig.app.databinding.FragmentMarketSelectedBinding
import com.hedvig.app.feature.chat.ui.ChatActivity
import com.hedvig.app.feature.denmark.ZignSecAuthenticationActivity
import com.hedvig.app.feature.marketing.service.MarketingTracker
import com.hedvig.app.feature.marketing.ui.MarketingActivity
import com.hedvig.app.feature.marketing.ui.MarketingViewModel
import com.hedvig.app.feature.webonboarding.WebOnboardingActivity
import com.hedvig.app.util.extensions.compatDrawable
import com.hedvig.app.util.extensions.getMarket
import com.hedvig.app.util.extensions.makeToast
import com.hedvig.app.util.extensions.storeBoolean
import com.hedvig.app.util.extensions.viewBinding
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.sharedViewModel

class MarketSelectedFragment : Fragment(R.layout.fragment_market_selected) {
    private val viewModel: MarketingViewModel by sharedViewModel()
    private val binding by viewBinding(FragmentMarketSelectedBinding::bind)
    private val tracker: MarketingTracker by inject()
    private val marketProvider: MarketProvider by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        requireContext().storeBoolean(MarketingActivity.SHOULD_OPEN_MARKET_SELECTED, true)

        binding.apply {
            legal.doOnApplyWindowInsets { view, insets, initialState ->
                view.updateMargin(bottom = initialState.margins.bottom + insets.systemWindowInsetBottom)
            }

            flag.doOnApplyWindowInsets { view, insets, initialState ->
                view.updateMargin(top = initialState.margins.top + insets.systemWindowInsetTop)
            }

            val market = requireContext().getMarket()
            if (market == null) {
                startActivity(MarketingActivity.newInstance(requireContext()))
                return
            }

            flag.apply {
                marketProvider.market?.let { market ->
                    setImageDrawable(context.compatDrawable(market.flag))
                }
                setHapticClickListener {
                    viewModel.navigateTo(
                        CurrentFragment.MARKET_PICKER,
                        signUp to "marketButton"
                    )
                }
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
                    Market.DK -> {
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
                    Market.DK -> {
                        startActivity(ZignSecAuthenticationActivity.newInstance(requireContext()))
                    }
                }
            }
        }
    }
}
