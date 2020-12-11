package com.hedvig.app.testdata.feature.onboarding.builders

import com.hedvig.android.owldroid.graphql.ChoosePlanQuery
import com.hedvig.android.owldroid.type.EmbarkStoryType

class EmbarkStoryBuilder(
    val name: String = ENGLISH_COMBO,
    val title: String = "Bundle",
    val description: String = "Get your price",
    val metadata: List<ChoosePlanQuery.Metadatum> = listOf(
        ChoosePlanQuery.Metadatum(
            asEmbarkStoryMetadataEntryDiscount = ChoosePlanQuery.AsEmbarkStoryMetadataEntryDiscount(
                discount = "25%"
            ),
            asEmbarkStoryMetaDataEntryWebUrlPath = ChoosePlanQuery.AsEmbarkStoryMetaDataEntryWebUrlPath(
                path = ENGLISH_COMBO_WEB_PATH
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

    companion object {
        const val ENGLISH_COMBO = "Web Onboarding NO - English Combo"
        const val ENGLISH_CONTENTS = "Web Onboarding NO - English Contents"
        const val ENGLISH_TRAVEL = "Web Onboarding NO - English Travel"

        const val ENGLISH_COMBO_WEB_PATH = "/no-en/new-member/combo"
        const val ENGLISH_CONTENTS_WEB_PATH = "/no-en/new-member/contents"
        const val ENGLISH_TRAVEL_WEB_PATH = "/no-en/new-member/travel"
    }
}
