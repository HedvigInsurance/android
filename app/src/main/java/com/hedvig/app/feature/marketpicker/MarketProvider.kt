package com.hedvig.app.feature.marketpicker

import android.content.Context
import androidx.fragment.app.FragmentManager
import com.hedvig.app.authenticate.AuthenticateDialog
import com.hedvig.app.feature.norway.NorwegianAuthenticationActivity
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
}

class MarketProviderImpl(
    private val context: Context
) : MarketProvider() {
    override val market
        get() = context.getMarket()
}
