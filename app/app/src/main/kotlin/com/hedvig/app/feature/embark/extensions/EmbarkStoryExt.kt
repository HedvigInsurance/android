package com.hedvig.app.feature.embark.extensions

import giraffe.EmbarkStoryQuery

fun EmbarkStoryQuery.EmbarkStory.getComputedValues() = computedStoreValues
  ?.associateBy({ it.key }, { it.value })
  ?: emptyMap()
