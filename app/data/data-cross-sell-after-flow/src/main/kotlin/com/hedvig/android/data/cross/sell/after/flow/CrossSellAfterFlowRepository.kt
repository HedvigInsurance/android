package com.hedvig.android.data.cross.sell.after.flow

import com.hedvig.android.core.tracking.ActionType
import com.hedvig.android.core.tracking.logAction
import com.hedvig.android.logger.logcat
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

interface CrossSellAfterFlowRepository {
  fun shouldShowCrossSellSheetWithInfo(): Flow<CrossSellInfoType?>

  fun completedCrossSellTriggeringSelfServiceSuccessfully(type: CrossSellInfoType)

  fun showedCrossSellSheet(type: CrossSellInfoType?)
}

sealed interface CrossSellInfoType {
  val loggableName: String
  val attributes: Map<String, Any?>?
    get() = null

  data object Home : CrossSellInfoType {
    override val loggableName: String = "home"
  }

  data class Claim(
    val info: ClaimInfo,
  ) : CrossSellInfoType {
    override val loggableName: String = "claim"
    override val attributes: Map<String, Any?>? = with(info) {
      buildMap {
        this.put("id", id)
        this.put("status", status)
        this.put("type", type)
        if (typeOfContract != null) {
          this.put("typeOfContract", typeOfContract)
        }
      }
    }

    data class ClaimInfo(
      val id: String,
      val status: String,
      val type: String,
      val typeOfContract: String?,
    )
  }

  data object ChangeTier : CrossSellInfoType {
    override val loggableName: String = "changeTier"
  }

  data object Addon : CrossSellInfoType {
    override val loggableName: String = "addon"
  }

  data object EditCoInsured : CrossSellInfoType {
    override val loggableName: String = "coInsured"
  }

  data object ClaimFlow : CrossSellInfoType {
    override val loggableName: String = "moveFlow"
  }

  data object MovingFlow : CrossSellInfoType {
    override val loggableName: String = "moveFlow"
  }
}

class CrossSellAfterFlowRepositoryImpl() : CrossSellAfterFlowRepository {
  /**
   * Purposefully not stored in persistent storage so that if the app is killed after this was set, we do not still
   * show the cross sells again.
   */
  private val shouldShowCrossSellSheet: MutableStateFlow<CrossSellInfoType?> = MutableStateFlow(null)

  override fun shouldShowCrossSellSheetWithInfo(): Flow<CrossSellInfoType?> = shouldShowCrossSellSheet

  override fun completedCrossSellTriggeringSelfServiceSuccessfully(type: CrossSellInfoType) {
    logcat { "CrossSellAfterFlowRepository: completedCrossSellTriggeringSelfServiceSuccessfully" }
    shouldShowCrossSellSheet.value = type
  }

  override fun showedCrossSellSheet(type: CrossSellInfoType?) {
    logcat { "CrossSellAfterFlowRepository: showedCrossSellSheet" }
    if (type != null) {
      logAction(
        ActionType.CUSTOM,
        type.loggableName,
        type.attributes ?: emptyMap(),
      )
    }
    shouldShowCrossSellSheet.value = null
  }
}
