package com.hedvig.app.testdata.dashboard.builders

import com.hedvig.android.owldroid.fragment.AddressFragment
import com.hedvig.android.owldroid.fragment.ContractStatusFragment
import com.hedvig.android.owldroid.fragment.InsurableLimitsFragment
import com.hedvig.android.owldroid.fragment.PerilFragment
import com.hedvig.android.owldroid.graphql.InsuranceQuery
import com.hedvig.android.owldroid.type.SwedishApartmentLineOfBusiness
import com.hedvig.android.owldroid.type.TypeOfContract
import com.hedvig.app.testdata.common.ContractStatus
import java.time.LocalDate

class InsuranceDataBuilder(
    private val contracts: List<ContractStatus> = emptyList(),
    private val typeOfContract: TypeOfContract = TypeOfContract.SE_APARTMENT_BRF,
    private val renewal: InsuranceQuery.UpcomingRenewal? =
        InsuranceQuery.UpcomingRenewal(
            renewalDate = LocalDate.now(),
            draftCertificateUrl = "https://www.example.com"
        ),
    private val displayName: String = "Hemförsäkring"
) {

    fun build() = InsuranceQuery.Data(
        contracts = contracts.map { c ->
            InsuranceQuery.Contract(
                id = "120e9ac9-84b1-4e5d-add1-70a9bad340be",
                status = InsuranceQuery.Status(
                    fragments = InsuranceQuery.Status.Fragments(
                        contractStatusFragment = ContractStatusFragment(
                            asPendingStatus = if (c == ContractStatus.PENDING) {
                                ContractStatusFragment.AsPendingStatus(
                                    pendingSince = null
                                )
                            } else {
                                null
                            },
                            asActiveInFutureStatus = when (c) {
                                ContractStatus.ACTIVE_IN_FUTURE -> ContractStatusFragment.AsActiveInFutureStatus(
                                    futureInception = LocalDate.of(2025, 1, 1)
                                )
                                ContractStatus.ACTIVE_IN_FUTURE_INVALID -> ContractStatusFragment.AsActiveInFutureStatus(
                                    futureInception = null
                                )
                                else -> null
                            },
                            asActiveStatus = if (c == ContractStatus.ACTIVE) {
                                ContractStatusFragment.AsActiveStatus(
                                    pastInception = LocalDate.now()
                                )
                            } else {
                                null
                            },
                            asActiveInFutureAndTerminatedInFutureStatus = if (c == ContractStatus.ACTIVE_IN_FUTURE_AND_TERMINATED_IN_FUTURE) {
                                ContractStatusFragment.AsActiveInFutureAndTerminatedInFutureStatus(
                                    futureInception = LocalDate.of(2024, 1, 1),
                                    futureTermination = LocalDate.of(2034, 1, 1)
                                )
                            } else {
                                null
                            },
                            asTerminatedInFutureStatus = null,
                            asTerminatedTodayStatus = if (c == ContractStatus.TERMINATED_TODAY) {
                                ContractStatusFragment.AsTerminatedTodayStatus(today = LocalDate.now())
                            } else {
                                null
                            },
                            asTerminatedStatus = if (c == ContractStatus.TERMINATED) {
                                ContractStatusFragment.AsTerminatedStatus(
                                    termination = null
                                )
                            } else {
                                null
                            }
                        )
                    )
                ),
                displayName = displayName,
                typeOfContract = typeOfContract,
                upcomingRenewal = renewal,
                currentAgreement = InsuranceQuery.CurrentAgreement(
                    asAgreementCore = InsuranceQuery.AsAgreementCore(
                        certificateUrl = "https://www.example.com"
                    ),
                    asSwedishApartmentAgreement = InsuranceQuery.AsSwedishApartmentAgreement(
                        address = InsuranceQuery.Address(
                            fragments = InsuranceQuery.Address.Fragments(
                                AddressFragment(
                                    street = "Testvägen 1",
                                    postalCode = "123 45",
                                    city = "Tensta"
                                )
                            )
                        ),
                        numberCoInsured = 2,
                        squareMeters = 50,
                        saType = SwedishApartmentLineOfBusiness.RENT
                    ),
                    asNorwegianHomeContentAgreement = null,
                    asNorwegianTravelAgreement = null,
                    asSwedishHouseAgreement = null,
                    asDanishHomeContentAgreement = null
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
        }
    )
}
