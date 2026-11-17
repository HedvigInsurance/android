package com.hedvig.android.ui.contract

import com.hedvig.android.data.contract.ContractGroup
import hedvigandroid.ui_contract.generated.resources.Res
import hedvigandroid.ui_contract.generated.resources.accident
import hedvigandroid.ui_contract.generated.resources.car
import hedvigandroid.ui_contract.generated.resources.cat
import hedvigandroid.ui_contract.generated.resources.dog
import hedvigandroid.ui_contract.generated.resources.home
import hedvigandroid.ui_contract.generated.resources.homeowner
import hedvigandroid.ui_contract.generated.resources.rental
import hedvigandroid.ui_contract.generated.resources.safety
import hedvigandroid.ui_contract.generated.resources.student
import hedvigandroid.ui_contract.generated.resources.vacation
import hedvigandroid.ui_contract.generated.resources.villa
import org.jetbrains.compose.resources.DrawableResource

fun ContractGroup.gradientResource(): DrawableResource = when (this) {
  ContractGroup.HOMEOWNER -> Res.drawable.homeowner
  ContractGroup.HOUSE -> Res.drawable.villa
  ContractGroup.RENTAL -> Res.drawable.rental
  ContractGroup.STUDENT -> Res.drawable.student
  ContractGroup.ACCIDENT -> Res.drawable.accident
  ContractGroup.CAR -> Res.drawable.car
  ContractGroup.CAT -> Res.drawable.cat
  ContractGroup.DOG -> Res.drawable.dog
  ContractGroup.TRAVEL -> Res.drawable.homeowner
  ContractGroup.COUNTRY_HOME -> Res.drawable.vacation
  ContractGroup.UNKNOWN -> Res.drawable.home
  ContractGroup.QASA_LANDLORD -> Res.drawable.home
  ContractGroup.PAYMENT_PROTECTION -> Res.drawable.safety
}

fun ContractGroup.pillowResource(): DrawableResource = when (this) {
  ContractGroup.HOMEOWNER -> Res.drawable.homeowner
  ContractGroup.HOUSE -> Res.drawable.villa
  ContractGroup.RENTAL -> Res.drawable.rental
  ContractGroup.STUDENT -> Res.drawable.student
  ContractGroup.ACCIDENT -> Res.drawable.accident
  ContractGroup.CAR -> Res.drawable.car
  ContractGroup.CAT -> Res.drawable.cat
  ContractGroup.DOG -> Res.drawable.dog
  ContractGroup.TRAVEL -> Res.drawable.homeowner
  ContractGroup.COUNTRY_HOME -> Res.drawable.vacation
  ContractGroup.UNKNOWN -> Res.drawable.home
  ContractGroup.QASA_LANDLORD -> Res.drawable.home
  ContractGroup.PAYMENT_PROTECTION -> Res.drawable.safety
}

/**
 * A pillow illustration exposed for callers that need one without an owning contract, e.g. the home
 * add-on banners, which carry no imagery from the backend. The drawable resources are internal to
 * this module, so callers pick a pillow through this enum instead.
 */
enum class PillowType {
  CAR,
  VACATION,
}

fun PillowType.pillowResource(): DrawableResource = when (this) {
  PillowType.CAR -> Res.drawable.car
  PillowType.VACATION -> Res.drawable.vacation
}
