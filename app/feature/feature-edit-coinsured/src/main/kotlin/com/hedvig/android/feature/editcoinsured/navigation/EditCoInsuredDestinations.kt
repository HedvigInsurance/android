package com.hedvig.android.feature.editcoinsured.navigation

import com.hedvig.android.navigation.common.Destination
import com.hedvig.android.navigation.common.DestinationNavTypeAware
import kotlin.reflect.KType
import kotlin.reflect.typeOf
import kotlinx.datetime.LocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

sealed interface EditCoInsuredDestination : Destination {
  @Serializable
  data class CoInsuredAddInfo(val contractId: String) : Destination

  @Serializable
  data class CoInsuredAddOrRemove(val contractId: String) : Destination

  @Serializable
  data class EditCoInsuredTriage(
    @SerialName("contractId")
    val contractId: String? = null,
  ) : Destination

  @Serializable
  data class Success(val date: LocalDate) : Destination {
    companion object : DestinationNavTypeAware {
      override val typeList: List<KType> = listOf(typeOf<LocalDate>())
    }
  }
}
