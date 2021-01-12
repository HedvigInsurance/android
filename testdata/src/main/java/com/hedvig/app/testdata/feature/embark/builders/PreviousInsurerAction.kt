package com.hedvig.app.testdata.feature.embark.builders

import com.hedvig.android.owldroid.graphql.EmbarkStoryQuery
import com.hedvig.android.owldroid.type.EmbarkPreviousInsuranceProviderActionDataProviders

object PreviousInsurerAction {
    fun build() = EmbarkStoryQuery.Action(
        asEmbarkSelectAction = null,
        asEmbarkTextAction = null,
        asEmbarkTextActionSet = null,
        asEmbarkPreviousInsuranceProviderAction = EmbarkStoryQuery.AsEmbarkPreviousInsuranceProviderAction(
            data = EmbarkStoryQuery.Data5(providers = EmbarkPreviousInsuranceProviderActionDataProviders.SWEDISH)
        )
    )
}
