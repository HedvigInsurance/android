package com.hedvig.app.feature.embark

import com.hedvig.android.owldroid.graphql.EmbarkStoryQuery

fun EmbarkStoryQuery.EmbarkStory.getComputedValues() = computedStoreValues
    ?.associateBy({ it.key }, { it.value })
    ?: emptyMap()
