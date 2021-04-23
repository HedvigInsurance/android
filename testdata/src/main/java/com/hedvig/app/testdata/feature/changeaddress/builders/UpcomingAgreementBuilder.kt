package com.hedvig.app.testdata.feature.changeaddress.builders

import com.hedvig.android.owldroid.fragment.AddressFragment
import com.hedvig.android.owldroid.fragment.UpcomingAgreementFragment
import com.hedvig.android.owldroid.graphql.UpcomingAgreementQuery
import com.hedvig.android.owldroid.type.SwedishApartmentLineOfBusiness
import java.time.LocalDate

class UpcomingAgreementBuilder {

    fun build() = UpcomingAgreementQuery.UpcomingAgreementChange(
        fragments = UpcomingAgreementQuery.UpcomingAgreementChange.Fragments(
            upcomingAgreementFragment = UpcomingAgreementFragment(
                newAgreement = UpcomingAgreementFragment.NewAgreement(
                    asSwedishApartmentAgreement = UpcomingAgreementFragment.AsSwedishApartmentAgreement(
                        address = UpcomingAgreementFragment.Address(
                            fragments = UpcomingAgreementFragment.Address.Fragments(
                                addressFragment = AddressFragment(
                                    street = "Test Street 123",
                                    postalCode = "123 TEST",
                                    city = "Test City"
                                )
                            )
                        ),
                        squareMeters = 50,
                        activeFrom = LocalDate.of(2021, 4, 11),
                        saType = SwedishApartmentLineOfBusiness.RENT
                    ),
                    asDanishHomeContentAgreement = null,
                    asNorwegianHomeContentAgreement = null,
                    asSwedishHouseAgreement = null
                )
            )
        )
    )
}
