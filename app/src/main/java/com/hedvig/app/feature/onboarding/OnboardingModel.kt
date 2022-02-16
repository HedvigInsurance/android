package com.hedvig.app.feature.onboarding

sealed class OnboardingModel {

    data class BundleItem(
        val selected: Boolean,
        val bundle: BundlesResult.Success.Bundle
    ) : OnboardingModel()

    object Error : OnboardingModel()
}
