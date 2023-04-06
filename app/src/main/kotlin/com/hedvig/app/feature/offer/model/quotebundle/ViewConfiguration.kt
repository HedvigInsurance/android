package com.hedvig.app.feature.offer.model.quotebundle

import com.hedvig.android.core.ui.insurance.GradientType
import com.hedvig.app.util.extensions.gradient
import giraffe.fragment.QuoteBundleFragment
import giraffe.type.QuoteBundleAppConfigurationStartDateTerminology
import giraffe.type.QuoteBundleAppConfigurationTitle

data class ViewConfiguration(
  val showCampaignManagement: Boolean,
  val showFAQ: Boolean,
  val ignoreCampaigns: Boolean,
  val title: Title,
  val startDateTerminology: StartDateTerminology,
  val gradient: GradientType,
  val postSignScreen: PostSignScreen,
) {
  enum class Title {
    LOGO, UPDATE, UNKNOWN;
  }

  enum class StartDateTerminology {
    START_DATE, ACCESS_DATE, UNKNOWN
  }
}

fun QuoteBundleFragment.toViewConfiguration() = ViewConfiguration(
  showCampaignManagement = appConfiguration.showCampaignManagement,
  showFAQ = appConfiguration.showFAQ,
  ignoreCampaigns = appConfiguration.ignoreCampaigns,
  title = appConfiguration.title.toTitle(),
  startDateTerminology = appConfiguration.startDateTerminology.toStartDateTerminology(),
  gradient = quotes.first().typeOfContract.gradient(),
  postSignScreen = PostSignScreen.from(appConfiguration.postSignStep),
)

private fun QuoteBundleAppConfigurationTitle.toTitle() = when (this) {
  QuoteBundleAppConfigurationTitle.LOGO -> ViewConfiguration.Title.LOGO
  QuoteBundleAppConfigurationTitle.UPDATE_SUMMARY -> ViewConfiguration.Title.UPDATE
  QuoteBundleAppConfigurationTitle.UNKNOWN__ -> ViewConfiguration.Title.UNKNOWN
}

private fun QuoteBundleAppConfigurationStartDateTerminology.toStartDateTerminology() = when (this) {
  QuoteBundleAppConfigurationStartDateTerminology.START_DATE -> ViewConfiguration.StartDateTerminology.START_DATE
  QuoteBundleAppConfigurationStartDateTerminology.ACCESS_DATE -> ViewConfiguration.StartDateTerminology.ACCESS_DATE
  QuoteBundleAppConfigurationStartDateTerminology.UNKNOWN__ -> ViewConfiguration.StartDateTerminology.UNKNOWN
}
