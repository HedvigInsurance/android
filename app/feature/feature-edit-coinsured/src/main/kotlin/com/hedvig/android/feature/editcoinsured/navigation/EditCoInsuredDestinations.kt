package com.hedvig.android.feature.editcoinsured.navigation

import com.hedvig.android.data.coinsured.CoInsuredFlowType
import com.hedvig.android.feature.editcoinsured.data.InsuranceForEditOrAddCoInsured
import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.navigation.common.NavKeyTypeAware
import kotlin.reflect.KType
import kotlin.reflect.typeOf
import kotlinx.datetime.LocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

sealed interface EditCoInsuredDestination : HedvigNavKey {
  @Serializable
  data class CoInsuredAddInfo(
    val contractId: String,
    val type: CoInsuredFlowType,
  ) : HedvigNavKey

  @Serializable
  data class CoInsuredAddOrRemove(
    val contractId: String,
    val type: CoInsuredFlowType,
  ) : HedvigNavKey

  @Serializable
  data class EditCoInsuredTriage(
    @SerialName("contractId")
    val contractId: String? = null,
    val type: CoInsuredFlowType = CoInsuredFlowType.CoInsured,
  ) : HedvigNavKey

  @Serializable
  data class EditCoOwnersTriageDeepLink(
    @SerialName("contractId")
    val contractId: String? = null,
  ) : HedvigNavKey

  @Serializable
  data class Success(val date: LocalDate, val type: CoInsuredFlowType) : HedvigNavKey {
    companion object : NavKeyTypeAware {
      override val typeList: List<KType> = listOf(typeOf<LocalDate>())
    }
  }
}
