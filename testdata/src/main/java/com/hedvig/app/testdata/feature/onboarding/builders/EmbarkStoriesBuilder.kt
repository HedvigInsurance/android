package com.hedvig.app.testdata.feature.onboarding.builders

import com.hedvig.android.owldroid.graphql.ChoosePlanQuery
import com.hedvig.app.testdata.feature.onboarding.builders.EmbarkStoryBuilder.Companion.ENGLISH_COMBO
import com.hedvig.app.testdata.feature.onboarding.builders.EmbarkStoryBuilder.Companion.ENGLISH_COMBO_WEB_PATH
import com.hedvig.app.testdata.feature.onboarding.builders.EmbarkStoryBuilder.Companion.ENGLISH_CONTENTS
import com.hedvig.app.testdata.feature.onboarding.builders.EmbarkStoryBuilder.Companion.ENGLISH_CONTENTS_WEB_PATH
import com.hedvig.app.testdata.feature.onboarding.builders.EmbarkStoryBuilder.Companion.ENGLISH_TRAVEL
import com.hedvig.app.testdata.feature.onboarding.builders.EmbarkStoryBuilder.Companion.ENGLISH_TRAVEL_WEB_PATH

class EmbarkStoriesBuilder(
    val list: List<ChoosePlanQuery.EmbarkStory> = listOf(
        EmbarkStoryBuilder(
            name = ENGLISH_COMBO,
            title = "Bundle",
            metadata = listOf(
                ChoosePlanQuery.Metadatum(
                    asEmbarkStoryMetadataEntryDiscount = ChoosePlanQuery.AsEmbarkStoryMetadataEntryDiscount(
                        discount = "25%"
                    ),
                    asEmbarkStoryMetaDataEntryWebUrlPath = ChoosePlanQuery.AsEmbarkStoryMetaDataEntryWebUrlPath(
                        path = ENGLISH_COMBO_WEB_PATH
                    )
                )
            )
        ).build(),
        EmbarkStoryBuilder(
            name = ENGLISH_CONTENTS,
            title = "Content",
            metadata = listOf(
                ChoosePlanQuery.Metadatum(
                    asEmbarkStoryMetadataEntryDiscount = null,
                    asEmbarkStoryMetaDataEntryWebUrlPath = ChoosePlanQuery.AsEmbarkStoryMetaDataEntryWebUrlPath(
                        path = ENGLISH_CONTENTS_WEB_PATH
                    )
                )
            )
        ).build(),
        EmbarkStoryBuilder(
            name = ENGLISH_TRAVEL,
            title = "Travel",
            metadata = listOf(
                ChoosePlanQuery.Metadatum(
                    asEmbarkStoryMetadataEntryDiscount = null,
                    asEmbarkStoryMetaDataEntryWebUrlPath = ChoosePlanQuery.AsEmbarkStoryMetaDataEntryWebUrlPath(
                        path = ENGLISH_TRAVEL_WEB_PATH
                    )
                )
            )
        ).build()
    )
) {
    fun build() = ChoosePlanQuery.Data(embarkStories = list)
}
