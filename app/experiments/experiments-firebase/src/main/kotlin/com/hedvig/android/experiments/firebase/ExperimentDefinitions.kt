package com.hedvig.android.experiments.firebase

/**
 * An experiment the app knows how to read out of Remote Config.
 *
 * Firebase A/B Testing never tells the SDK which experiment or variant a device is in. It only sets the Remote
 * Config parameters each variant overrides. So the experiment's name is fixed here, and the parameter holds the
 * variant: a string in SCREAMING_SNAKE_CASE (`CONTROL`, `DISCOUNT`, ...). A blank value, which is also the in-code
 * default, means the member is not in the experiment and nothing is sent for it.
 */
internal data class ExperimentDefinition(
  val name: String,
  val remoteConfigKey: String,
)

internal val knownExperiments: List<ExperimentDefinition> = listOf(
  ExperimentDefinition(name = "ACCIDENT_DISCOUNT", remoteConfigKey = "accident_discount_variant"),
)
