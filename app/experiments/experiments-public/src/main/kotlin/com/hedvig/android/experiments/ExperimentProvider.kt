package com.hedvig.android.experiments

/**
 * An A/B test the member is bucketed into, in the shape the backend's `experiments` inputs take: both values in
 * SCREAMING_SNAKE_CASE.
 */
data class Experiment(
  val name: String,
  val variant: String,
)

interface ExperimentProvider {
  /**
   * The experiments this member is currently in, to attach to cross-sell queries so the backend can decide what to
   * offer. Waits briefly for the startup fetch to finish and then answers from whatever is active, so a query is
   * delayed a little at most and never blocked. Empty when the member is in no experiment.
   */
  suspend fun activeExperiments(): List<Experiment>
}
