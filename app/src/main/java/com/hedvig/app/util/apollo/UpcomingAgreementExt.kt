package com.hedvig.app.util.apollo

import com.hedvig.android.owldroid.fragment.UpcomingAgreementChangeFragment
import com.hedvig.android.owldroid.fragment.UpcomingAgreementFragment
import com.hedvig.app.feature.home.ui.changeaddress.GetUpcomingAgreementUseCase.UpcomingAgreementResult.UpcomingAgreement
import com.hedvig.app.feature.table.intoTable
import java.time.LocalDate

fun UpcomingAgreementFragment.toUpcomingAgreementResult(): UpcomingAgreement? {
    if (status.newAgreement() == null) {
        return null
    }

    return UpcomingAgreement(
        activeFrom = activeFrom(),
        table = upcomingAgreementDetailsTable.fragments.tableFragment.intoTable()
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
