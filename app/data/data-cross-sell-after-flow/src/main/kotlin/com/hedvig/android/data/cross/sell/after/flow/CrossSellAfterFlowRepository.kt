package com.hedvig.android.data.cross.sell.after.flow

import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.core.tracking.ActionType
import com.hedvig.android.core.tracking.logAction
import com.hedvig.android.logger.logcat
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

interface CrossSellAfterFlowRepository {
  fun shouldShowCrossSellSheetWithInfo(): Flow<CrossSellInfoType?>

  fun completedCrossSellTriggeringSelfServiceSuccessfully(type: CrossSellInfoType)

  fun showedCrossSellSheet(type: CrossSellInfoType?)
}

sealed class CrossSellInfoType() {
  abstract val source: String

  abstract val contractId: String?
  protected abstract val extraInfo: Map<String, Any?>?
  val attributes: Map<String, Any?>
    get() = buildMap {
      this.put("source", source)
      if (extraInfo != null) {
        this.put("info", extraInfo)
      }
    }

  data class ClosedClaim(
    val info: ClaimInfo,
    override val contractId: String?
  ) : CrossSellInfoType() {
    override val source: String = "closedClaim"
    override val extraInfo: Map<String, Any?> = with(info) {
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

  data class ChangeTier(
    override val contractId: String?
  ) : CrossSellInfoType() {
    override val source: String = "changeTier"
    override val extraInfo: Map<String, Any?>? = null
  }

  data object Addon : CrossSellInfoType() {
    override val source: String = "addon"
    override val extraInfo: Map<String, Any?>? = null
    override val contractId: String? = null
  }

  data object EditCoInsured : CrossSellInfoType() {
    override val source: String = "editCoInsured"
    override val extraInfo: Map<String, Any?>? = null
    override val contractId: String? = null
  }

  data class MovingFlow(
    override val contractId: String?
  ) : CrossSellInfoType() {
    override val source: String = "movingFlow"
    override val extraInfo: Map<String, Any?>? = null
  }
}

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
@Inject
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
      val actionName = "crossSell"
      logAction(
        ActionType.CUSTOM,
        actionName,
        type.attributes,
      )
    }
    shouldShowCrossSellSheet.value = null
  }
}
