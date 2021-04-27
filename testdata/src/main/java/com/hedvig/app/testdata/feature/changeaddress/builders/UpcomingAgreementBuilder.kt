package com.hedvig.app.testdata.feature.changeaddress.builders

import com.hedvig.android.owldroid.fragment.AddressFragment
import com.hedvig.android.owldroid.fragment.UpcomingAgreementFragment
import com.hedvig.android.owldroid.graphql.UpcomingAgreementQuery
import com.hedvig.android.owldroid.type.SwedishApartmentLineOfBusiness
import java.time.LocalDate

class UpcomingAgreementBuilder(
    private val newAgreement: UpcomingAgreementFragment.NewAgreement
) {

    fun build() = UpcomingAgreementQuery.UpcomingAgreementChange(
        fragments = UpcomingAgreementQuery.UpcomingAgreementChange.Fragments(
            upcomingAgreementFragment = UpcomingAgreementFragment(
                newAgreement = newAgreement
            )
        )
    )
}
