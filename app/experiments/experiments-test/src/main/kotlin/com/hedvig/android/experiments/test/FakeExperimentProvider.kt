package com.hedvig.android.experiments.test

import com.hedvig.android.experiments.Experiment
import com.hedvig.android.experiments.ExperimentProvider

class FakeExperimentProvider(
  var experiments: List<Experiment> = emptyList(),
) : ExperimentProvider {
  override suspend fun activeExperiments(): List<Experiment> = experiments
}
