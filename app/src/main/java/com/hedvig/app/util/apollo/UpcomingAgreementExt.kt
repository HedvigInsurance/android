package com.hedvig.app.util.apollo

import com.hedvig.android.owldroid.fragment.UpcomingAgreementChangeFragment
import com.hedvig.android.owldroid.fragment.UpcomingAgreementFragment
import com.hedvig.app.feature.home.ui.changeaddress.GetUpcomingAgreementUseCase
import com.hedvig.app.feature.table.intoTable
import java.time.LocalDate

fun UpcomingAgreementFragment.toUpcomingAgreementResult(): GetUpcomingAgreementUseCase
.UpcomingAgreementResult.UpcomingAgreement? {
    if (status.newAgreement() == null) {
        return null
    }

    return GetUpcomingAgreementUseCase.UpcomingAgreementResult.UpcomingAgreement(
        activeFrom = activeFrom(),
        address = address(),
        table = upcomingAgreementDetailsTable.fragments.tableFragment.intoTable()
    )
}

private fun UpcomingAgreementFragment.activeFrom(): LocalDate? {
    val newAgreement = status.newAgreement()

    return newAgreement?.asNorwegianHomeContentAgreement?.activeFrom
        ?: newAgreement?.asDanishHomeContentAgreement?.activeFrom
        ?: newAgreement?.asSwedishApartmentAgreement?.activeFrom
        ?: newAgreement?.asSwedishHouseAgreement?.activeFrom
}

private fun UpcomingAgreementFragment.address(): String? {
    val newAgreement = status.newAgreement()

    return newAgreement?.asNorwegianHomeContentAgreement?.address?.fragments?.addressFragment?.street
        ?: newAgreement?.asDanishHomeContentAgreement?.address?.fragments?.addressFragment?.street
        ?: newAgreement?.asSwedishApartmentAgreement?.address?.fragments?.addressFragment?.street
        ?: newAgreement?.asSwedishHouseAgreement?.address?.fragments?.addressFragment?.street
}

private fun UpcomingAgreementFragment.Status.newAgreement(): UpcomingAgreementChangeFragment.NewAgreement? {
    return asActiveStatus?.upcomingAgreementChange?.fragments?.upcomingAgreementChangeFragment?.newAgreement
        ?: asTerminatedInFutureStatus?.upcomingAgreementChange?.fragments?.upcomingAgreementChangeFragment?.newAgreement
        ?: asTerminatedTodayStatus?.upcomingAgreementChange?.fragments?.upcomingAgreementChangeFragment?.newAgreement
}
