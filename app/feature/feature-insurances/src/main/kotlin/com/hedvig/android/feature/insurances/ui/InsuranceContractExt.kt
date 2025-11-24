package com.hedvig.android.feature.insurances.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import com.hedvig.android.data.contract.android.toDrawableRes
import com.hedvig.android.data.contract.isTrialContract
import com.hedvig.android.design.system.hedvig.ChipType.GENERAL
import com.hedvig.android.design.system.hedvig.ChipType.TIER
import com.hedvig.android.design.system.hedvig.ChipUiData
import com.hedvig.android.feature.insurances.data.InsuranceContract
import hedvig.resources.Res
import hedvig.resources.CONTRACTS_TRIAL_TERMINATION_DATE_MESSAGE
import hedvig.resources.CONTRACTS_TRIAL_TERMINATION_DATE_MESSAGE_TOMORROW
import hedvig.resources.CONTRACT_STATUS_ACTIVE_IN_FUTURE
import hedvig.resources.CONTRACT_STATUS_PENDING
import hedvig.resources.CONTRACT_STATUS_TERMINATED
import hedvig.resources.CONTRACT_STATUS_TERMINATED_TODAY
import hedvig.resources.CONTRACT_STATUS_TO_BE_TERMINATED
import hedvig.resources.DASHBOARD_INSURANCE_STATUS_ACTIVE
import hedvig.resources.DASHBOARD_INSURANCE_STATUS_ACTIVE_UPDATE_DATE
import kotlin.time.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun InsuranceContract.createChips(): List<ChipUiData> {
  val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
  val listOfChips =
    when (this) {
      is InsuranceContract.PendingInsuranceContract -> {
        val text = stringResource(Res.string.CONTRACT_STATUS_PENDING)
        listOfNotNull(
          tierName?.let {
            ChipUiData(chipText = it, chipType = TIER)
          },
          ChipUiData(chipText = text, chipType = GENERAL),
        )
      }
      is InsuranceContract.EstablishedInsuranceContract -> listOfNotNull(
        tierName?.let {
          ChipUiData(chipText = it, chipType = TIER)
        },
        terminationDate?.let { terminationDate ->
          val text = if (terminationDate == today) {
            if (currentInsuranceAgreement.productVariant.contractType.isTrialContract()) {
              stringResource(Res.string.CONTRACTS_TRIAL_TERMINATION_DATE_MESSAGE_TOMORROW)
            } else {
              stringResource(Res.string.CONTRACT_STATUS_TERMINATED_TODAY)
            }
          } else if (terminationDate < today) {
            stringResource(Res.string.CONTRACT_STATUS_TERMINATED)
          } else {
            if (currentInsuranceAgreement.productVariant.contractType.isTrialContract()) {
              stringResource(Res.string.CONTRACTS_TRIAL_TERMINATION_DATE_MESSAGE, terminationDate)
            } else {
              stringResource(Res.string.CONTRACT_STATUS_TO_BE_TERMINATED, terminationDate)
            }
          }
          ChipUiData(text, GENERAL)
        },
        upcomingInsuranceAgreement?.activeFrom?.let { activeFromDate ->
          val text = stringResource(Res.string.DASHBOARD_INSURANCE_STATUS_ACTIVE_UPDATE_DATE, activeFromDate)
          ChipUiData(text, GENERAL)
        },
        inceptionDate.let { inceptionDate ->
          if (inceptionDate > today) {
            val text = stringResource(Res.string.CONTRACT_STATUS_ACTIVE_IN_FUTURE, inceptionDate)
            ChipUiData(text, GENERAL)
          } else if (terminationDate == null) {
            val text = stringResource(Res.string.DASHBOARD_INSURANCE_STATUS_ACTIVE)
            ChipUiData(text, GENERAL)
          } else {
            null
          }
        },
      )
    }
  return listOfChips
}

@Composable
internal fun InsuranceContract.createPainter(): Painter {
  val productVariant = when (this) {
    is InsuranceContract.PendingInsuranceContract -> productVariant
    is InsuranceContract.EstablishedInsuranceContract -> currentInsuranceAgreement.productVariant
  }
  return when (this) {
    is InsuranceContract.EstablishedInsuranceContract if isTerminated ->
      ColorPainter(Color.Black.copy(alpha = 0.7f))
    else ->
      productVariant.contractGroup
        .toDrawableRes()
        .let { drawableRes -> painterResource(drawableRes) }
  }
}
