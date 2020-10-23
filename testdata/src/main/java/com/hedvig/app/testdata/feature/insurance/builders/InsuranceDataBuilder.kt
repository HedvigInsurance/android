package com.hedvig.app.testdata.feature.insurance.builders

import com.hedvig.android.owldroid.fragment.AddressFragment
import com.hedvig.android.owldroid.fragment.ContractStatusFragment
import com.hedvig.android.owldroid.fragment.InsurableLimitsFragment
import com.hedvig.android.owldroid.fragment.PerilFragment
import com.hedvig.android.owldroid.graphql.InsuranceQuery
import com.hedvig.android.owldroid.type.NorwegianHomeContentLineOfBusiness
import com.hedvig.android.owldroid.type.SwedishApartmentLineOfBusiness
import com.hedvig.android.owldroid.type.TypeOfContract
import java.time.LocalDate

class InsuranceDataBuilder(
    private val type: TypeOfContract = TypeOfContract.SE_APARTMENT_RENT,
    private val coinsured: Int = 2,
    private val renewal: InsuranceQuery.UpcomingRenewal? =
        InsuranceQuery.UpcomingRenewal(
            renewalDate = LocalDate.now(),
            draftCertificateUrl = "https://www.example.com"
        ),
) {

    fun build() = InsuranceQuery.Data(
        contracts = listOf(
            InsuranceQuery.Contract(
                id = "120e9ac9-84b1-4e5d-add1-70a9bad340be",
                status = InsuranceQuery.Status(
                    fragments = InsuranceQuery.Status.Fragments(
                        contractStatusFragment = ContractStatusFragment(
                            asPendingStatus = null,
                            asActiveInFutureStatus = null,
                            asActiveStatus = ContractStatusFragment.AsActiveStatus(
                                pastInception = LocalDate.of(2020, 2, 1)
                            ),
                            asActiveInFutureAndTerminatedInFutureStatus = null,
                            asTerminatedInFutureStatus = null,
                            asTerminatedTodayStatus = null,
                            asTerminatedStatus = null
                        )
                    )
                ),
                displayName = "Hemförsäkring",
                typeOfContract = type,
                upcomingRenewal = renewal,
                currentAgreement = InsuranceQuery.CurrentAgreement(
                    asAgreementCore = InsuranceQuery.AsAgreementCore(
                        certificateUrl = "https://www.example.com"
                    ),
                    asSwedishApartmentAgreement = if (type == TypeOfContract.SE_APARTMENT_RENT) {
                        InsuranceQuery.AsSwedishApartmentAgreement(
                            address = InsuranceQuery.Address(
                                fragments = InsuranceQuery.Address.Fragments(
                                    AddressFragment(
                                        street = "Testvägen 1",
                                        postalCode = "123 45",
                                        city = "Tensta"
                                    )
                                )
                            ),
                            numberCoInsured = coinsured,
                            squareMeters = 50,
                            saType = SwedishApartmentLineOfBusiness.RENT
                        )
                    } else {
                        null
                    },
                    asNorwegianHomeContentAgreement = if (type == TypeOfContract.NO_HOME_CONTENT_RENT) {
                        InsuranceQuery.AsNorwegianHomeContentAgreement(
                            address = InsuranceQuery.Address2(
                                fragments = InsuranceQuery.Address2.Fragments(
                                    AddressFragment(
                                        street = "Testvägen 1",
                                        postalCode = "123 45",
                                        city = "Tensta"
                                    )
                                )
                            ),
                            numberCoInsured = coinsured,
                            squareMeters = 50,
                            nhcType = NorwegianHomeContentLineOfBusiness.RENT
                        )
                    } else {
                        null
                    },
                    asNorwegianTravelAgreement = if (type == TypeOfContract.NO_TRAVEL) {
                        InsuranceQuery.AsNorwegianTravelAgreement(
                            numberCoInsured = coinsured
                        )
                    } else {
                        null
                    },
                    asSwedishHouseAgreement = if (type == TypeOfContract.SE_HOUSE) {
                        InsuranceQuery.AsSwedishHouseAgreement(
                            address = InsuranceQuery.Address1(
                                fragments = InsuranceQuery.Address1.Fragments(
                                    AddressFragment(
                                        street = "Testvägen 1",
                                        postalCode = "123 45",
                                        city = "Tensta",
                                    )
                                )
                            ),
                            numberCoInsured = coinsured,
                            squareMeters = 50,
                        )
                    } else {
                        null
                    },
                    asDanishHomeContentAgreement = if (type == TypeOfContract.DK_HOME_CONTENT) {
                        InsuranceQuery.AsDanishHomeContentAgreement(
                            address = InsuranceQuery.Address3(
                                fragments = InsuranceQuery.Address3.Fragments(
                                    AddressFragment(
                                        street = "Testvägen 1",
                                        postalCode = "123 45",
                                        city = "Tensta",
                                    )
                                )
                            )
                        )
                    } else {
                        null
                    },
                ),
                perils = listOf(
                    InsuranceQuery.Peril(
                        fragments = InsuranceQuery.Peril.Fragments(
                            PerilFragment(
                                title = "Mock",
                                description = "Mock",
                                icon = PerilFragment.Icon(
                                    variants = PerilFragment.Variants(
                                        dark = PerilFragment.Dark(
                                            svgUrl = "/app-content-service/fire_dark.svg"
                                        ),
                                        light = PerilFragment.Light(
                                            svgUrl = "/app-content-service/fire.svg"
                                        )
                                    )
                                )
                            )
                        )

                    ),
                    InsuranceQuery.Peril(
                        fragments = InsuranceQuery.Peril.Fragments(
                            PerilFragment(
                                title = "Mock",
                                description = "Mock",
                                icon = PerilFragment.Icon(
                                    variants = PerilFragment.Variants(
                                        dark = PerilFragment.Dark(
                                            svgUrl = "/app-content-service/fire_dark.svg"
                                        ),
                                        light = PerilFragment.Light(
                                            svgUrl = "/app-content-service/fire.svg"
                                        )
                                    )
                                )
                            )
                        )

                    ),
                    InsuranceQuery.Peril(
                        fragments = InsuranceQuery.Peril.Fragments(
                            PerilFragment(
                                title = "Mock",
                                description = "Mock",
                                icon = PerilFragment.Icon(
                                    variants = PerilFragment.Variants(
                                        dark = PerilFragment.Dark(
                                            svgUrl = "/app-content-service/fire_dark.svg"
                                        ),
                                        light = PerilFragment.Light(
                                            svgUrl = "/app-content-service/fire.svg"
                                        )
                                    )
                                )
                            )
                        )

                    ),
                    InsuranceQuery.Peril(
                        fragments = InsuranceQuery.Peril.Fragments(
                            PerilFragment(
                                title = "Mock",
                                description = "Mock",
                                icon = PerilFragment.Icon(
                                    variants = PerilFragment.Variants(
                                        dark = PerilFragment.Dark(
                                            svgUrl = "/app-content-service/fire_dark.svg"
                                        ),
                                        light = PerilFragment.Light(
                                            svgUrl = "/app-content-service/fire.svg"
                                        )
                                    )
                                )
                            )
                        )

                    ),
                    InsuranceQuery.Peril(
                        fragments = InsuranceQuery.Peril.Fragments(
                            PerilFragment(
                                title = "Mock",
                                description = "Mock",
                                icon = PerilFragment.Icon(
                                    variants = PerilFragment.Variants(
                                        dark = PerilFragment.Dark(
                                            svgUrl = "/app-content-service/fire_dark.svg"
                                        ),
                                        light = PerilFragment.Light(
                                            svgUrl = "/app-content-service/fire.svg"
                                        )
                                    )
                                )
                            )
                        )

                    )
                ),
                insurableLimits = listOf(
                    InsuranceQuery.InsurableLimit(
                        fragments = InsuranceQuery.InsurableLimit.Fragments(
                            InsurableLimitsFragment(
                                label = "Utstyrene dine er forsikrat till",
                                limit = "1 000 000 kr",
                                description = "Dina prylar är försäkrade till"
                            )
                        )
                    )
                ),
                termsAndConditions = InsuranceQuery.TermsAndConditions(
                    displayName = "Terms and Conditions",
                    url = "https://cdn.hedvig.com/info/insurance-terms-tenant-owners-2019-05.pdf"
                )
            )
        )
    )
}
