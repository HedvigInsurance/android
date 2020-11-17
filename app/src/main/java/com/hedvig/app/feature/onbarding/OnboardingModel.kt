package com.hedvig.app.feature.onbarding

sealed class OnboardingModel {
    sealed class Quote : OnboardingModel() {
        data class Bundle(val selected: Boolean) : Quote()
        data class Content(val selected: Boolean) : Quote()
        data class Travel(val selected: Boolean) : Quote()
    }

    object Info : OnboardingModel()
    object Button : OnboardingModel()
}
