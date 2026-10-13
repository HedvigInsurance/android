package com.hedvig.android.experiments.firebase

/**
 * An experiment the app runs on its own members, switched on from Remote Config.
 *
 * Firebase's A/B Testing buckets by app install and binds one experiment to one app, so it is not used. Remote
 * Config instead serves one project-wide number per experiment, [remoteConfigKey]: the percentage of members, 0 to
 * 100, who get the [treatment] variant. Every other enrolled member gets [control], which is today's behaviour. 0,
 * also the in-code default, switches the experiment off: nothing is sent and the backend keeps today's behaviour.
 *
 * Which variant a member gets is a pure function of their member id, see [experimentBucket], so it is the same on
 * every device, after a reinstall, and on iOS. Raising the percentage only moves members from control to treatment,
 * never the other way, so a rollout can be ramped without reshuffling anyone.
 */
internal data class ExperimentDefinition(
  val name: String,
  val remoteConfigKey: String,
  val control: String,
  val treatment: String,
)

internal val knownExperiments: List<ExperimentDefinition> = listOf(
  // The accident cross-sell is discounted today. The treatment removes the discount to measure its effect.
  ExperimentDefinition(
    name = "ACCIDENT_DISCOUNT",
    remoteConfigKey = "accident_no_discount_percent",
    control = "CONTROL",
    treatment = "NO_DISCOUNT",
  ),
)
