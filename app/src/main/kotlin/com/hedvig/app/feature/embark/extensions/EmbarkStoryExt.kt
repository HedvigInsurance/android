package com.hedvig.app.feature.embark.extensions

import com.hedvig.android.apollo.graphql.EmbarkStoryQuery

fun EmbarkStoryQuery.EmbarkStory.getComputedValues() = computedStoreValues
  ?.associateBy({ it.key }, { it.value })
  ?: emptyMap()
