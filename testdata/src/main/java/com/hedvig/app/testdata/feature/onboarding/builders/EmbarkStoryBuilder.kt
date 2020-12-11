package com.hedvig.app.testdata.feature.onboarding.builders

import com.hedvig.android.owldroid.graphql.ChoosePlanQuery
import com.hedvig.android.owldroid.type.EmbarkStoryType

class EmbarkStoryBuilder(
    val name: String = "Bundle",
    val title: String = "Bundle",
    val description: String = "Get your price",
    val metadata: List<ChoosePlanQuery.Metadatum> = listOf(
        ChoosePlanQuery.Metadatum(
            asEmbarkStoryMetadataEntryDiscount = ChoosePlanQuery.AsEmbarkStoryMetadataEntryDiscount(
                discount = "25%"
            ),
            asEmbarkStoryMetaDataEntryWebUrlPath = ChoosePlanQuery.AsEmbarkStoryMetaDataEntryWebUrlPath(
                path = "/no-en/new-member/combo"
            )
        )
    )
) {
    fun build() = ChoosePlanQuery.EmbarkStory(
        name = name,
        title = title,
        type = EmbarkStoryType.APP_ONBOARDING,
        description = description,
        metadata = metadata
    )
}
