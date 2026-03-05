package com.hedvig.android.feature.editcoinsured.navigation

import com.hedvig.android.data.coinsured.CoInsuredFlowType
import com.hedvig.android.feature.editcoinsured.data.InsuranceForEditOrAddCoInsured
import com.hedvig.android.navigation.common.Destination
import com.hedvig.android.navigation.common.DestinationNavTypeAware
import kotlin.reflect.KType
import kotlin.reflect.typeOf
import kotlinx.datetime.LocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

sealed interface EditCoInsuredDestination : Destination {
  @Serializable
  data class CoInsuredAddInfo(
    val contractId: String,
    val type: CoInsuredFlowType,
  ) : Destination

  @Serializable
  data class CoInsuredAddOrRemove(
    val contractId: String,
    val type: CoInsuredFlowType,
  ) : Destination

  @Serializable
  data class EditCoInsuredTriage(
    @SerialName("contractId")
    val contractId: String? = null,
    val type: CoInsuredFlowType = CoInsuredFlowType.CoInsured,
  ) : Destination

  @Serializable
  data class Success(val date: LocalDate, val type: CoInsuredFlowType) : Destination {
    companion object : DestinationNavTypeAware {
      override val typeList: List<KType> = listOf(typeOf<LocalDate>())
    }
  }
}
