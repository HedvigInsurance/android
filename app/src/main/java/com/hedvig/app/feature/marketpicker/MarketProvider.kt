package com.hedvig.app.feature.marketpicker

import android.content.Context
import androidx.fragment.app.FragmentManager
import com.hedvig.app.authenticate.AuthenticateDialog
import com.hedvig.app.feature.norway.NorwegianAuthenticationActivity
import com.hedvig.app.HedvigApplication
import com.hedvig.app.shouldOverrideFeatureFlags
import com.hedvig.app.util.extensions.getMarket

abstract class MarketProvider {
    abstract val market: Market?
    
    fun openAuth(context: Context, fragmentManager: FragmentManager) {
        when (market) {
            Market.SE -> {
                AuthenticateDialog().show(fragmentManager, AuthenticateDialog.TAG)
            }
            Market.NO -> {
                context.startActivity(NorwegianAuthenticationActivity.newInstance(context))
            }
        }
    }
    abstract val enabledMarkets: List<Market>
}

class MarketProviderImpl(
    private val context: Context,
    private val app: HedvigApplication
) : MarketProvider() {
    override val market
        get() = context.getMarket()

    override val enabledMarkets = listOfNotNull(
        Market.SE,
        Market.NO,
        if (shouldOverrideFeatureFlags(app)) {
            Market.DK
        } else {
            null
        }
    )
}
