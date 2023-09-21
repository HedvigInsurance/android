package com.hedvig.android.feature.home.legacychangeaddress

import com.hedvig.android.core.common.android.table.intoTable
import giraffe.fragment.UpcomingAgreementChangeFragment
import giraffe.fragment.UpcomingAgreementFragment
import java.time.LocalDate

fun UpcomingAgreementFragment.toUpcomingAgreementResult():
  GetUpcomingAgreementUseCase.UpcomingAgreementResult.UpcomingAgreement? {
  if (status.newAgreement() == null) {
    return null
  }

  return GetUpcomingAgreementUseCase.UpcomingAgreementResult.UpcomingAgreement(
    activeFrom = activeFrom(),
    table = upcomingAgreementDetailsTable.fragments.tableFragment.intoTable(),
  )
}

private fun UpcomingAgreementFragment.activeFrom(): LocalDate? {
  val newAgreement = status.newAgreement()
  return newAgreement?.asAgreementCore?.activeFrom
}

private fun UpcomingAgreementFragment.Status.newAgreement(): UpcomingAgreementChangeFragment.NewAgreement? {
  return asActiveStatus?.upcomingAgreementChange?.fragments?.upcomingAgreementChangeFragment?.newAgreement
    ?: asTerminatedInFutureStatus?.upcomingAgreementChange?.fragments?.upcomingAgreementChangeFragment?.newAgreement
    ?: asTerminatedTodayStatus?.upcomingAgreementChange?.fragments?.upcomingAgreementChangeFragment?.newAgreement
}
