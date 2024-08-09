package com.hedvig.android.feature.insurances.ui

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.hedvig.android.data.contract.android.toDrawableRes
import com.hedvig.android.data.contract.isTrialContract
import com.hedvig.android.feature.insurances.data.InsuranceContract
import hedvig.resources.R
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

@Composable
internal fun InsuranceContract.createChips(): List<String> {
  val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
  return listOfNotNull(
    terminationDate?.let { terminationDate ->
      if (terminationDate == today) {
        if (currentInsuranceAgreement.productVariant.contractType.isTrialContract()) {
          stringResource(R.string.CONTRACTS_TRIAL_TERMINATION_DATE_MESSAGE_TOMORROW)
        } else {
          stringResource(R.string.CONTRACT_STATUS_TERMINATED_TODAY)
        }
      } else if (terminationDate < today) {
        stringResource(R.string.CONTRACT_STATUS_TERMINATED)
      } else {
        if (currentInsuranceAgreement.productVariant.contractType.isTrialContract()) {
          stringResource(R.string.CONTRACTS_TRIAL_TERMINATION_DATE_MESSAGE, terminationDate)
        } else {
          stringResource(R.string.CONTRACT_STATUS_TO_BE_TERMINATED, terminationDate)
        }
      }
    },
    upcomingInsuranceAgreement?.activeFrom?.let { activeFromDate ->
      stringResource(R.string.DASHBOARD_INSURANCE_STATUS_ACTIVE_UPDATE_DATE, activeFromDate)
    },
    inceptionDate.let { inceptionDate ->
      if (inceptionDate > today) {
        stringResource(R.string.CONTRACT_STATUS_ACTIVE_IN_FUTURE, inceptionDate)
      } else if (terminationDate == null) {
        stringResource(id = R.string.DASHBOARD_INSURANCE_STATUS_ACTIVE)
      } else {
        null
      }
    },
  )
}

@Composable
internal fun InsuranceContract.createPainter(): Painter {
  val painterResourceId = painterResourceId()
  return if (painterResourceId != null) {
    painterResource(id = painterResourceId)
  } else {
    ColorPainter(Color.Black.copy(alpha = 0.7f))
  }
}

@DrawableRes
internal fun InsuranceContract.painterResourceId(): Int? {
  return if (isTerminated) {
    null
  } else {
    currentInsuranceAgreement.productVariant.contractGroup.toDrawableRes()
  }
}
