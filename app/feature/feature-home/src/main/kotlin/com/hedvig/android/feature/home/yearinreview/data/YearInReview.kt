package com.hedvig.android.feature.home.yearinreview.data

internal data class YearInReview(
  val stories: List<YearInReviewStory>,
)

/**
 * One page of the year in review. Its text reads as one paragraph, with [leadText] emphasized ahead of
 * [text]. [imageUrl] may point to an animated GIF. With [showImageOnCard], the image is shown centered on a
 * square card, for an image that is not itself a picture filling its frame, such as a logo.
 */
internal data class YearInReviewStory(
  val id: String,
  val title: String?,
  val leadText: String,
  val text: String,
  val imageUrl: String,
  val showImageOnCard: Boolean = false,
)
