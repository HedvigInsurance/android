package com.hedvig.app.service.push

import android.app.Application
import android.content.Context
import com.hedvig.android.core.common.ApplicationScope
import com.hedvig.android.market.Market
import com.hedvig.android.market.MarketManager
import io.customer.messagingpush.ModuleMessagingPushFCM
import io.customer.sdk.CustomerIO
import io.customer.sdk.data.model.Region
import kotlinx.coroutines.launch
import com.hedvig.app.R
import kotlinx.coroutines.flow.distinctUntilChanged

private const val REQUEST_TIMEOUT_MILLIS = 8000L

class CustomerIoInitializer(
  private val marketManager: MarketManager,
  private val applicationScope: ApplicationScope,
) {

  private val region = Region.EU

  fun setupCustomerIo(application: Application) {
    applicationScope.launch {
      marketManager.observeMarket()
        .distinctUntilChanged()
        .collect { market ->
          if (market != null) {
            CustomerIO.Builder(
              siteId = getSideId(application.applicationContext, market),
              apiKey = getApiKey(application.applicationContext, market),
              appContext = application,
            ).apply {
              addCustomerIOModule(ModuleMessagingPushFCM())
              setRequestTimeout(REQUEST_TIMEOUT_MILLIS)
              setRegion(region)
              build()
            }
          }
        }
    }
  }

  private fun getSideId(context: Context, market: Market) = when (market) {
    Market.SE -> context.getString(R.string.CUSTOMERIO_SE_SITE_ID)
    Market.NO -> context.getString(R.string.CUSTOMERIO_NO_SITE_ID)
    Market.DK -> context.getString(R.string.CUSTOMERIO_DE_SITE_ID)
    Market.FR -> throw java.lang.IllegalArgumentException("No side id for FR")
  }

  private fun getApiKey(context: Context, market: Market) = when (market) {
    Market.SE -> context.getString(R.string.CUSTOMERIO_SE_API_KEY)
    Market.NO -> context.getString(R.string.CUSTOMERIO_NO_API_KEY)
    Market.DK -> context.getString(R.string.CUSTOMERIO_DE_API_KEY)
    Market.FR -> throw java.lang.IllegalArgumentException("No api key for FR")
  }
}
