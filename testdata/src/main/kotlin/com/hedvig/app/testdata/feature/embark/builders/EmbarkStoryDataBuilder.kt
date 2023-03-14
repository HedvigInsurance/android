package com.hedvig.app.testdata.feature.embark.builders

import giraffe.EmbarkStoryQuery

data class EmbarkStoryDataBuilder(
  private val startPassage: String = "1",
  private val passages: List<EmbarkStoryQuery.Passage> = emptyList(),
  private val computedStoreValues: List<EmbarkStoryQuery.ComputedStoreValue> = emptyList(),
) {
  fun build() = EmbarkStoryQuery.Data(
    embarkStory = EmbarkStoryQuery.EmbarkStory(
      startPassage = startPassage,
      passages = passages,
      computedStoreValues = computedStoreValues,
    ),
  )
}
