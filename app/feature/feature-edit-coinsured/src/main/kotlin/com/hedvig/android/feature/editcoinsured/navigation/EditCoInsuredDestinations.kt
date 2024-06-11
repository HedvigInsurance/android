package com.hedvig.android.feature.editcoinsured.navigation

import com.hedvig.android.navigation.compose.typeMapOf
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

sealed interface EditCoInsuredDestination {
  @Serializable
  data class Success(val date: LocalDate) {
    companion object {
      val typeMap = typeMapOf<LocalDate>()
    }
  }
}
