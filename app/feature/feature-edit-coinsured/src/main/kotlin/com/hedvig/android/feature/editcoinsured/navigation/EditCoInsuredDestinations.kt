package com.hedvig.android.feature.editcoinsured.navigation

import com.hedvig.android.navigation.compose.DestinationNavTypeAware
import kotlin.reflect.KType
import kotlin.reflect.typeOf
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

sealed interface EditCoInsuredDestination {
  @Serializable
  data class Success(val date: LocalDate) {
    companion object : DestinationNavTypeAware {
      override val typeList: List<KType> = listOf(typeOf<LocalDate>())
    }
  }
}
