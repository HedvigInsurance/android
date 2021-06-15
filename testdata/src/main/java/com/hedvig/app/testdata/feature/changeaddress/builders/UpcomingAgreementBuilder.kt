package com.hedvig.app.testdata.feature.changeaddress.builders

import com.hedvig.android.owldroid.fragment.AddressFragment
import com.hedvig.android.owldroid.fragment.TableFragment
import com.hedvig.android.owldroid.fragment.UpcomingAgreementChangeFragment
import com.hedvig.android.owldroid.fragment.UpcomingAgreementFragment
import com.hedvig.app.testdata.common.builders.TableFragmentBuilder
import java.time.LocalDate

class UpcomingAgreementBuilder(
    private val street: String = "Test Street 123",
    private val postalCode: String = "123 TEST",
    private val city: String = "Test City",
    private val activeFrom: LocalDate = LocalDate.of(2021, 4, 11),
    private val newAgreement: UpcomingAgreementChangeFragment.NewAgreement = UpcomingAgreementChangeFragment
        .NewAgreement(
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
    private val table: TableFragment = TableFragmentBuilder(
        title = "Details",
        sections = listOf(
            "Details" to listOf(
                Triple("Address", "Subtitle", "Testgatan 123")
            )
        )
    ).build(),
) {

    fun build() = UpcomingAgreementFragment(
        upcomingAgreementDetailsTable = UpcomingAgreementFragment.UpcomingAgreementDetailsTable(
            fragments = UpcomingAgreementFragment.UpcomingAgreementDetailsTable.Fragments(
                table
            )
        ),
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
