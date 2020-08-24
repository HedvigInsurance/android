package com.hedvig.app.testdata.dashboard.builders

import com.hedvig.android.owldroid.fragment.AddressFragment
import com.hedvig.android.owldroid.fragment.ContractStatusFragment
import com.hedvig.android.owldroid.fragment.InsurableLimitsFragment
import com.hedvig.android.owldroid.fragment.PerilFragment
import com.hedvig.android.owldroid.graphql.DashboardQuery
import com.hedvig.android.owldroid.type.SwedishApartmentLineOfBusiness
import com.hedvig.android.owldroid.type.TypeOfContract
import java.time.LocalDate

class DashboardDataBuilder(
    private val upcomingRenewal: DashboardQuery.UpcomingRenewal? = null
) {

    fun build() = DashboardQuery.Data(
        contracts = listOf(DashboardQuery.Contract(
            id = "120e9ac9-84b1-4e5d-add1-70a9bad340be",
            status = DashboardQuery.Status(
                fragments = DashboardQuery.Status.Fragments(
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
            typeOfContract = TypeOfContract.SE_APARTMENT_RENT,
            upcomingRenewal = upcomingRenewal,
            currentAgreement = DashboardQuery.CurrentAgreement(
                asAgreementCore = DashboardQuery.AsAgreementCore(
                    certificateUrl = "https://www.example.com"
                ),
                asSwedishApartmentAgreement = DashboardQuery.AsSwedishApartmentAgreement(
                    address = DashboardQuery.Address(
                        fragments = DashboardQuery.Address.Fragments(
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
                asSwedishHouseAgreement = null
            ),
            perils = listOf(
                DashboardQuery.Peril(
                    fragments = DashboardQuery.Peril.Fragments(
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
                DashboardQuery.Peril(
                    fragments = DashboardQuery.Peril.Fragments(
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
                DashboardQuery.Peril(
                    fragments = DashboardQuery.Peril.Fragments(
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
                DashboardQuery.Peril(
                    fragments = DashboardQuery.Peril.Fragments(
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
                DashboardQuery.Peril(
                    fragments = DashboardQuery.Peril.Fragments(
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
                DashboardQuery.InsurableLimit(
                    fragments = DashboardQuery.InsurableLimit.Fragments(
                        InsurableLimitsFragment(
                            label = "Utstyrene dine er forsikrat till",
                            limit = "1 000 000 kr",
                            description = "Dina prylar är försäkrade till"
                        )
                    )
                )
            ),
            termsAndConditions = DashboardQuery.TermsAndConditions(
                displayName = "Terms and Conditions",
                url = "https://cdn.hedvig.com/info/insurance-terms-tenant-owners-2019-05.pdf"
            )
        ))
    )
}
