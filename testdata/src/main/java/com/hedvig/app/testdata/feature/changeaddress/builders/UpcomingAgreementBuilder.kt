package com.hedvig.app.testdata.feature.changeaddress.builders

import com.hedvig.android.owldroid.fragment.AddressFragment
import com.hedvig.android.owldroid.fragment.UpcomingAgreementChangeFragment
import com.hedvig.android.owldroid.fragment.UpcomingAgreementFragment
import java.time.LocalDate

class UpcomingAgreementBuilder(
    private val street: String = "Test Street 123",
    private val postalCode: String = "123 TEST",
    private val city: String = "Test City",
    private val activeFrom: LocalDate = LocalDate.of(2021, 4, 11),
    private val newAgreement: UpcomingAgreementChangeFragment.NewAgreement = UpcomingAgreementChangeFragment.NewAgreement(
        asSwedishApartmentAgreement = UpcomingAgreementChangeFragment.AsSwedishApartmentAgreement(
            address = UpcomingAgreementChangeFragment.Address(
                fragments = UpcomingAgreementChangeFragment.Address.Fragments(
                    addressFragment = AddressFragment(
                        street = street,
                        postalCode = postalCode,
                        city = city
                    )
                )
            ),
            activeFrom = activeFrom,
        ),
        asDanishHomeContentAgreement = null,
        asNorwegianHomeContentAgreement = null,
        asSwedishHouseAgreement = null
    ),
    private val table: UpcomingAgreementFragment.UpcomingAgreementDetailsTable = UpcomingAgreementFragment.UpcomingAgreementDetailsTable(
        title = "Upcoming Agreement",
        sections = listOf(
            UpcomingAgreementFragment.Section(
                title = "Details",
                rows = listOf(
                    UpcomingAgreementFragment.Row(
                        title = "Address",
                        value = "Testgatan 123",
                        subtitle = "Subtitle"
                    )
                )
            )
        )
    )
) {

    fun build() = UpcomingAgreementFragment(
        upcomingAgreementDetailsTable = table,
        status = UpcomingAgreementFragment.Status(
            asActiveStatus = UpcomingAgreementFragment.AsActiveStatus(
                upcomingAgreementChange = UpcomingAgreementFragment.UpcomingAgreementChange(
                    fragments = UpcomingAgreementFragment.UpcomingAgreementChange.Fragments(
                        upcomingAgreementChangeFragment = UpcomingAgreementChangeFragment(
                            newAgreement = newAgreement
                        )
                    )
                )
            ),
            asTerminatedInFutureStatus = null,
            asTerminatedTodayStatus = null,
        )
    )
}
