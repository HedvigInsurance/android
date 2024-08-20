package com.hedvig.android.feature.insurances.ui

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import com.hedvig.android.data.contract.ContractGroup.ACCIDENT
import com.hedvig.android.data.contract.ContractGroup.CAR
import com.hedvig.android.data.contract.ContractGroup.CAT
import com.hedvig.android.data.contract.ContractGroup.DOG
import com.hedvig.android.data.contract.ContractGroup.HOMEOWNER
import com.hedvig.android.data.contract.ContractGroup.HOUSE
import com.hedvig.android.data.contract.ContractGroup.RENTAL
import com.hedvig.android.data.contract.ContractGroup.STUDENT
import com.hedvig.android.data.contract.ContractGroup.TRAVEL
import com.hedvig.android.data.contract.ContractGroup.UNKNOWN
import com.hedvig.android.data.contract.isTrialContract
import com.hedvig.android.feature.insurances.data.InsuranceContract
import com.hedvig.android.feature.insurances.ui.UiInsuranceContract.UiContractGroup.Accident
import com.hedvig.android.feature.insurances.ui.UiInsuranceContract.UiContractGroup.Car
import com.hedvig.android.feature.insurances.ui.UiInsuranceContract.UiContractGroup.Cat
import com.hedvig.android.feature.insurances.ui.UiInsuranceContract.UiContractGroup.Dog
import com.hedvig.android.feature.insurances.ui.UiInsuranceContract.UiContractGroup.Homeowner
import com.hedvig.android.feature.insurances.ui.UiInsuranceContract.UiContractGroup.House
import com.hedvig.android.feature.insurances.ui.UiInsuranceContract.UiContractGroup.Rental
import com.hedvig.android.feature.insurances.ui.UiInsuranceContract.UiContractGroup.Student
import com.hedvig.android.feature.insurances.ui.UiInsuranceContract.UiContractGroup.Travel
import com.hedvig.android.feature.insurances.ui.UiInsuranceContract.UiContractGroup.Unknown
import com.hedvig.android.feature.insurances.ui.UiInsuranceContract.UiInsuranceChipInfo
import com.hedvig.android.feature.insurances.ui.UiInsuranceContract.UiInsuranceChipInfo.Active
import com.hedvig.android.feature.insurances.ui.UiInsuranceContract.UiInsuranceChipInfo.Terminated
import com.hedvig.android.feature.insurances.ui.UiInsuranceContract.UiInsuranceChipInfo.TerminatesOn
import com.hedvig.android.feature.insurances.ui.UiInsuranceContract.UiInsuranceChipInfo.ToBeActivatedOn
import com.hedvig.android.feature.insurances.ui.UiInsuranceContract.UiInsuranceChipInfo.ToBeTerminatedAfterToday
import com.hedvig.android.feature.insurances.ui.UiInsuranceContract.UiInsuranceChipInfo.ToBeUpdatedOn
import com.hedvig.android.feature.insurances.ui.UiInsuranceContract.UiInsuranceChipInfo.ValidUntil
import com.hedvig.android.feature.insurances.ui.UiInsuranceContract.UiInsuranceChipInfo.ValidUntilTomorrow
import hedvig.resources.R
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlinx.serialization.Serializable

@Serializable
internal data class UiInsuranceContract(
  val id: String,
  val chips: List<UiInsuranceChipInfo>,
  val uiContractGroup: UiContractGroup,
  val displayName: String,
  val exposureDisplayName: String,
  val isTerminated: Boolean,
) {
  @Serializable
  internal sealed interface UiInsuranceChipInfo {
    data object ValidUntilTomorrow : UiInsuranceChipInfo

    data object ToBeTerminatedAfterToday : UiInsuranceChipInfo

    data object Terminated : UiInsuranceChipInfo

    data class ValidUntil(val date: LocalDate) : UiInsuranceChipInfo

    data class TerminatesOn(val date: LocalDate) : UiInsuranceChipInfo

    data class ToBeUpdatedOn(val date: LocalDate) : UiInsuranceChipInfo

    data class ToBeActivatedOn(val date: LocalDate) : UiInsuranceChipInfo

    data object Active : UiInsuranceChipInfo
  }

  internal enum class UiContractGroup {
    Homeowner,
    House,
    Rental,
    Student,
    Accident,
    Car,
    Cat,
    Dog,
    Travel,
    Unknown,
  }

  companion object {
    fun fromInsuranceContract(insuranceContract: InsuranceContract): UiInsuranceContract {
      val uiContractGroup = insuranceContract
        .currentInsuranceAgreement
        .productVariant
        .contractGroup
        .let { contractGroup ->
          when (contractGroup) {
            HOMEOWNER -> UiContractGroup.Homeowner
            RENTAL -> UiContractGroup.Rental
            ACCIDENT -> UiContractGroup.Accident
            HOUSE -> UiContractGroup.House
            TRAVEL -> UiContractGroup.Travel
            CAR -> UiContractGroup.Car
            CAT -> UiContractGroup.Cat
            DOG -> UiContractGroup.Dog
            STUDENT -> UiContractGroup.Student
            UNKNOWN -> UiContractGroup.Unknown
          }
        }
      return UiInsuranceContract(
        chips = insuranceContract.createChips(),
        uiContractGroup = uiContractGroup,
        id = insuranceContract.id,
        displayName = insuranceContract.currentInsuranceAgreement.productVariant.displayName,
        exposureDisplayName = insuranceContract.exposureDisplayName,
        isTerminated = insuranceContract.isTerminated,
      )
    }
  }
}

@StringRes
internal fun UiInsuranceChipInfo.toStringResource(): Int {
  return when (this) {
    ValidUntilTomorrow -> R.string.CONTRACTS_TRIAL_TERMINATION_DATE_MESSAGE_TOMORROW
    ToBeTerminatedAfterToday -> R.string.CONTRACT_STATUS_TERMINATED_TODAY
    Terminated -> R.string.CONTRACT_STATUS_TERMINATED
    is ValidUntil -> R.string.CONTRACTS_TRIAL_TERMINATION_DATE_MESSAGE
    is TerminatesOn -> R.string.CONTRACT_STATUS_TO_BE_TERMINATED
    is ToBeUpdatedOn -> R.string.DASHBOARD_INSURANCE_STATUS_ACTIVE_UPDATE_DATE
    is ToBeActivatedOn -> R.string.CONTRACT_STATUS_ACTIVE_IN_FUTURE
    Active -> R.string.DASHBOARD_INSURANCE_STATUS_ACTIVE
  }
}

@Composable
internal fun UiInsuranceContract.contractGroupCardPainter(): Painter? {
  return if (isTerminated) {
    null
  } else {
    uiContractGroup.contractGroupCardPainter()
  }
}

@Composable
internal fun UiInsuranceContract.UiContractGroup.contractGroupCardPainter(): Painter {
  val painterResourceId = toDrawableRes()
  return painterResource(id = painterResourceId)
}

internal fun InsuranceContract.createChips(): List<UiInsuranceChipInfo> {
  val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
  return listOfNotNull(
    terminationDate?.let { terminationDate ->
      if (terminationDate == today) {
        if (currentInsuranceAgreement.productVariant.contractType.isTrialContract()) {
          ValidUntilTomorrow
        } else {
          ToBeTerminatedAfterToday
        }
      } else if (terminationDate < today) {
        Terminated
      } else {
        if (currentInsuranceAgreement.productVariant.contractType.isTrialContract()) {
          ValidUntil(terminationDate)
        } else {
          TerminatesOn(terminationDate)
        }
      }
    },
    upcomingInsuranceAgreement?.activeFrom?.let { activeFromDate ->
      ToBeUpdatedOn(activeFromDate)
    },
    inceptionDate.let { inceptionDate ->
      if (inceptionDate > today) {
        ToBeActivatedOn(inceptionDate)
      } else if (terminationDate == null) {
        Active
      } else {
        null
      }
    },
  )
}

private fun UiInsuranceContract.UiContractGroup.toDrawableRes(): Int = when (this) {
  Homeowner -> R.drawable.gradient_homeowner
  House -> R.drawable.gradient_villa
  Rental -> R.drawable.gradient_rental
  Student -> R.drawable.gradient_student
  Accident -> R.drawable.gradient_accident
  Car -> R.drawable.gradient_car
  Cat -> R.drawable.gradient_cat
  Dog -> R.drawable.gradient_dog
  Travel -> R.drawable.gradient_homeowner
  Unknown -> R.drawable.gradient_homeowner
}
