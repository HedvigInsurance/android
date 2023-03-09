package com.hedvig.android.feature.terminateinsurance.data

import kotlinx.datetime.LocalDate

sealed interface TerminationStep {
  data class Date(
    val minDate: LocalDate,
    val maxDate: LocalDate,
  ) : TerminationStep

  data class Success(
    val terminationDate: LocalDate,
    val surveyUrl: String,
  ) : TerminationStep

  data class Failed(
    val message: String?,
  ) : TerminationStep
}
