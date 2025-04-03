package com.hedvig.android.data.cross.sell.after.flow

import com.hedvig.android.logger.logcat
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

interface CrossSellAfterFlowRepository {
  fun shouldShowCrossSellSheet(): Flow<Boolean>

  fun completedCrossSellTriggeringSelfServiceSuccessfully()

  fun showedCrossSellSheet()
}

class CrossSellAfterFlowRepositoryImpl() : CrossSellAfterFlowRepository {
  /**
   * Purposefully not stored in persistent storage so that if the app is killed after this was set, we do not still
   * show the cross sells again.
   */
  private val shouldShowCrossSellSheet = MutableStateFlow(false)

  override fun shouldShowCrossSellSheet(): Flow<Boolean> = shouldShowCrossSellSheet

  override fun completedCrossSellTriggeringSelfServiceSuccessfully() {
    logcat { "CrossSellAfterFlowRepository: completedCrossSellTriggeringSelfServiceSuccessfully" }
    shouldShowCrossSellSheet.value = true
  }

  override fun showedCrossSellSheet() {
    logcat { "CrossSellAfterFlowRepository: showedCrossSellSheet" }
    shouldShowCrossSellSheet.value = false
  }
}
