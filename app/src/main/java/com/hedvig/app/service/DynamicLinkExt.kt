package com.hedvig.app.service

import android.content.Context
import android.content.Intent
import com.hedvig.app.feature.loggedin.ui.LoggedInActivity
import com.hedvig.app.feature.loggedin.ui.LoggedInTabs
import com.hedvig.app.feature.marketing.MarketingActivity
import com.hedvig.app.feature.payment.connectPayinIntent
import com.hedvig.app.feature.referrals.ReferralsReceiverActivity
import com.hedvig.app.feature.settings.Market
import com.hedvig.app.feature.settings.MarketManager
import com.hedvig.app.util.featureflags.FeatureManager

suspend fun DynamicLink.startActivity(
  context: Context,
  marketManager: MarketManager,
  featureManager: FeatureManager,
  onDefault: () -> Unit,
) {
  when (this) {
    DynamicLink.DirectDebit -> {
      val market = marketManager.market ?: return onDefault()
      context.startActivities(
        arrayOf(
          Intent(context, LoggedInActivity::class.java),
          connectPayinIntent(
            context,
            featureManager.getPaymentType(),
            market,
            false,
          ),
        ),
      )
    }
    DynamicLink.Forever -> context.startActivity(
      LoggedInActivity.newInstance(
        context,
        initialTab = LoggedInTabs.REFERRALS,
      ),
    )
    DynamicLink.Insurance -> context.startActivity(
      LoggedInActivity.newInstance(
        context,
        initialTab = LoggedInTabs.INSURANCE,
      ),
    )
    is DynamicLink.Referrals -> {
      when (marketManager.market) {
        null -> context.startActivity(MarketingActivity.newInstance(context))
        Market.SE -> {
          context.startActivity(
            ReferralsReceiverActivity.newInstance(
              context,
              code,
              incentive,
            ),
            null,
          )
        }
        else -> context.startActivity(Intent(context, MarketingActivity::class.java))
      }
    }
    DynamicLink.None -> onDefault()
    DynamicLink.Unknown -> onDefault()
  }
}
