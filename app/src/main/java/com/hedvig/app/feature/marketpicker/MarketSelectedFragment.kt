package com.hedvig.app.feature.marketpicker

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.hedvig.app.R
import com.hedvig.app.databinding.FragmentMarketSelectedBinding
import com.hedvig.app.feature.marketing.service.MarketingTracker
import com.hedvig.app.feature.marketing.ui.MarketingActivity
import com.hedvig.app.feature.marketing.ui.MarketingViewModel
import com.hedvig.app.feature.marketing.ui.NavigationState
import com.hedvig.app.feature.settings.MarketManager
import com.hedvig.app.util.extensions.compatDrawable
import com.hedvig.app.util.extensions.view.applyNavigationBarInsetsMargin
import com.hedvig.app.util.extensions.view.applyStatusBarInsetsMargin
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class MarketSelectedFragment : Fragment(R.layout.fragment_market_selected) {
    private val viewModel: MarketingViewModel by sharedViewModel()
    private val binding by viewBinding(FragmentMarketSelectedBinding::bind)
    private val tracker: MarketingTracker by inject()
    private val marketManager: MarketManager by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.apply {
            logIn.applyNavigationBarInsetsMargin()
            flag.applyStatusBarInsetsMargin()

            val market = marketManager.market
            if (market == null) {
                startActivity(MarketingActivity.newInstance(requireContext()))
                return
            }

            flag.apply {
                setImageDrawable(context.compatDrawable(market.flag))
                setHapticClickListener {
                    viewModel.navigateTo(
                        NavigationState(
                            destination = CurrentFragment.MARKET_PICKER,
                            sharedElements = listOf(signUp to MarketingActivity.SHARED_ELEMENT_NAME),
                            reorderingAllowed = true,
                            addToBackStack = true
                        )
                    )
                }
            }

            signUp.setHapticClickListener {
                tracker.signUp()
                marketManager.market?.onboarding(requireContext())?.let { startActivity(it) }
            }

            logIn.setHapticClickListener {
                tracker.logIn()
                marketManager.market?.openAuth(requireContext(), parentFragmentManager)
            }
        }
    }
}
