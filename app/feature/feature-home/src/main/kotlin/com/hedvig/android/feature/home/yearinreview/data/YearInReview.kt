package com.hedvig.android.feature.home.yearinreview.data

import com.hedvig.android.data.contract.PillowType

internal data class YearInReview(
  val stories: List<YearInReviewStory>,
)

/**
 * One page of the year in review. Its text reads as one paragraph: [leadText], then [text] set apart from it,
 * then [closingText] if there is one, styled like [leadText]. [imageUrl] may point to an animated GIF, or be
 * null for a story without an image. With [showImageOnCard], the image is shown centered on a square card,
 * for an image that is not itself a picture filling its frame, such as a logo. [backgroundPillow] is the
 * pillow drawn behind the story; without one the screen picks one by the story's position.
 */
internal data class YearInReviewStory(
  val id: String,
  val title: String?,
  val leadText: String,
  val text: String,
  val closingText: String? = null,
  val imageUrl: String?,
  val showImageOnCard: Boolean = false,
  val backgroundPillow: PillowType? = null,
)
