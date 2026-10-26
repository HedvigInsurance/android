package com.hedvig.android.experiments.unleash

/**
 * An experiment whose groups are assigned by an Unleash flag with variants.
 *
 * Unleash does the bucketing: the flag's gradual rollout and its variants are sticky on the `memberId` context
 * field, so a member gets the same variant on every device and on iOS. The app only reads the variant and reports
 * it under [name], the name the backend's `experiments` input expects. The flag being off, or the member not being
 * in its rollout, means the member is in no experiment and nothing is sent.
 */
internal data class ExperimentDefinition(
  val name: String,
  val unleashFlag: String,
)

internal val knownExperiments: List<ExperimentDefinition> = listOf(
  // Variants DISCOUNT_50 (today's behaviour) and NO_DISCOUNT. The backend withholds the discount for NO_DISCOUNT.
  ExperimentDefinition(name = "ACCIDENT_DISCOUNT", unleashFlag = "accident_discount_experiment"),
)
