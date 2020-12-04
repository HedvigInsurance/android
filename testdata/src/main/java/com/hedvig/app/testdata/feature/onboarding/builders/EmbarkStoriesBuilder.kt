package com.hedvig.app.testdata.feature.onboarding.builders

import com.hedvig.android.owldroid.graphql.ChoosePlanQuery

class EmbarkStoriesBuilder(
    val list: List<ChoosePlanQuery.EmbarkStory> = listOf(
        EmbarkStoryBuilder(
            name = "Combo",
            title = "Bundle"
        ).build(),
        EmbarkStoryBuilder(
            name = "Contents",
            title = "Content"
        ).build(),
        EmbarkStoryBuilder(
            name = "Travel",
            title = "Travel"
        ).build()
    )
) {
    fun build() = list
}
