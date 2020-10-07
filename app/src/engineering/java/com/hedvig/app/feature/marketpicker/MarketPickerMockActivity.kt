package com.hedvig.app.feature.marketpicker

import android.os.Bundle
import com.hedvig.app.MockActivity
import com.hedvig.app.feature.marketing.ui.MarketingActivity
import com.hedvig.app.genericDevelopmentAdapter
import com.hedvig.app.util.extensions.getMarket
import com.hedvig.app.util.extensions.setMarket
import org.koin.core.module.Module

class MarketPickerMockActivity : MockActivity() {
    override val original = emptyList<Module>()
    override val mocks = emptyList<Module>()
    private var originalMarket: Market? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        originalMarket = getMarket()
    }

    override fun adapter() = genericDevelopmentAdapter {
        header("Market Picker")
        clickableItem("Market: SE") {
            setMarket(Market.SE)
            startActivity(
                MarketingActivity.newInstance(this@MarketPickerMockActivity)
            )
        }
        clickableItem("Market: NO") {
            setMarket(Market.NO)
            startActivity(
                MarketingActivity.newInstance(this@MarketPickerMockActivity)
            )
        }
        clickableItem("Market: not selected") {
            setMarket(null)
            startActivity(
                MarketingActivity.newInstance(this@MarketPickerMockActivity)
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        setMarket(originalMarket)
    }
}
