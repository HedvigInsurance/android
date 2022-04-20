package com.hedvig.app.feature.offer.model.quotebundle

import com.hedvig.android.owldroid.fragment.QuoteBundleFragment
import com.hedvig.android.owldroid.type.QuoteBundleAppConfigurationStartDateTerminology
import com.hedvig.android.owldroid.type.QuoteBundleAppConfigurationTitle
import com.hedvig.android.owldroid.type.TypeOfContractGradientOption

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

fun QuoteBundleFragment.AppConfiguration.toViewConfiguration() = ViewConfiguration(
    showCampaignManagement = showCampaignManagement,
    showFAQ = showFAQ,
    ignoreCampaigns = ignoreCampaigns,
    title = title.toTitle(),
    startDateTerminology = startDateTerminology.toStartDateTerminology(),
    gradient = gradientOption.toGradient(),
    postSignScreen = PostSignScreen.from(postSignStep)
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

private fun TypeOfContractGradientOption.toGradient() = when (this) {
    TypeOfContractGradientOption.GRADIENT_ONE -> GradientType.FALL_SUNSET
    TypeOfContractGradientOption.GRADIENT_TWO -> GradientType.SPRING_FOG
    TypeOfContractGradientOption.GRADIENT_THREE -> GradientType.SUMMER_SKY
    TypeOfContractGradientOption.GRADIENT_FOUR -> GradientType.SPRING_FOG // todo map the new gradient to another type?
    TypeOfContractGradientOption.UNKNOWN__ -> GradientType.SPRING_FOG
    TypeOfContractGradientOption.GRADIENT_FOUR -> GradientType.SPRING_FOG
}
