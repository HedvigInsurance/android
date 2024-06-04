package com.hedvig.android.feature.editcoinsured.navigation

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

sealed interface EditCoInsuredDestination : Destination {
  @Serializable
  data class Success(
    val date: LocalDate,
  ) : Destination
}
