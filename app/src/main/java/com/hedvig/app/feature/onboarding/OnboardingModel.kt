package com.hedvig.app.feature.onboarding

import com.hedvig.android.owldroid.graphql.ChoosePlanQuery

sealed class OnboardingModel {

    data class Bundle(
        val selected: Boolean,
        val embarkStory: ChoosePlanQuery.EmbarkStory
    ) : OnboardingModel()

    object Error : OnboardingModel()
}
