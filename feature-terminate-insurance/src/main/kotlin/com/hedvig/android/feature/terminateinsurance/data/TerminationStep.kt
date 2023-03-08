package com.hedvig.android.feature.terminateinsurance.data

import kotlinx.datetime.LocalDate

sealed interface TerminationStep {
  class Date(
    val minDate: LocalDate,
    val maxDate: LocalDate?,
  ) : TerminationStep

  class Success(
    val terminationDate: LocalDate,
    val surveyUrl: String,
  ) : TerminationStep

  class Failed(
    val message: String?,
  ) : TerminationStep
}
