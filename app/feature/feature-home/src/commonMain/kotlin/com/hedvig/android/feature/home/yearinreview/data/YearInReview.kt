package com.hedvig.android.feature.home.yearinreview.data

internal data class YearInReview(
  val stories: List<YearInReviewStory>,
)

/**
 * One page of the year in review. [imageUrl] may point to an animated GIF, or be null for a story without
 * an image.
 */
internal data class YearInReviewStory(
  val id: String,
  val title: String,
  val description: String,
  val imageUrl: String?,
  val action: YearInReviewAction?,
)

/** A link the story offers, shown as a button. */
internal data class YearInReviewAction(
  val title: String,
  val url: String,
)
