package com.hedvig.android.feature.home.yearinreview.data

internal data class YearInReview(
  val stories: List<YearInReviewStory>,
)

/** One page of the year in review. */
internal data class YearInReviewStory(
  val id: String,
  val title: String,
  val description: String,
  val media: YearInReviewMedia?,
  val action: YearInReviewAction?,
)

internal sealed interface YearInReviewMedia {
  val url: String

  /** A still or animated image, such as a GIF. */
  data class Image(override val url: String) : YearInReviewMedia

  data class Video(override val url: String) : YearInReviewMedia
}

/** A link the story offers, shown as a button. */
internal data class YearInReviewAction(
  val title: String,
  val url: String,
)
