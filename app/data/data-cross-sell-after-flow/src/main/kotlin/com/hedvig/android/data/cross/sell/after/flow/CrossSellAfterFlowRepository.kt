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

sealed class CrossSellInfoType() {
  abstract val loggableName: String
  protected abstract val extraInfo: Map<String, Any?>?
  val attributes: Map<String, Any?>
    get() = buildMap {
      this.put("type", loggableName)
      if (extraInfo != null) {
        this.put("info", extraInfo)
      }
    }

  data class ClosedClaim(
    val info: ClaimInfo,
  ) : CrossSellInfoType() {
    override val loggableName: String = "claim"
    override val extraInfo: Map<String, Any?>? = with(info) {
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
      val status: String?,
      val type: String?,
      val typeOfContract: String?,
    )
  }

  data object ChangeTier : CrossSellInfoType() {
    override val loggableName: String = "changeTier"
    override val extraInfo: Map<String, Any?>? = null
  }

  data object Addon : CrossSellInfoType() {
    override val loggableName: String = "addon"
    override val extraInfo: Map<String, Any?>? = null
  }

  data object EditCoInsured : CrossSellInfoType() {
    override val loggableName: String = "editCoInsured"
    override val extraInfo: Map<String, Any?>? = null
  }

  data object MovingFlow : CrossSellInfoType() {
    override val loggableName: String = "moveFlow"
    override val extraInfo: Map<String, Any?>? = null
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
    logcat { "CrossSellAfterFlowRepository: showedCrossSellSheet type:$type" }
    if (type != null) {
      logAction(
        ActionType.CUSTOM,
        type.loggableName,
        type.attributes,
      )
    }
    shouldShowCrossSellSheet.value = null
  }
}
