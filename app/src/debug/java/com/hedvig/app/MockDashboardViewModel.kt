package com.hedvig.app

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.hedvig.android.owldroid.fragment.AddressFragment
import com.hedvig.android.owldroid.fragment.ContractStatusFragment
import com.hedvig.android.owldroid.fragment.InsurableLimitsFragment
import com.hedvig.android.owldroid.fragment.PerilFragment
import com.hedvig.android.owldroid.graphql.DashboardQuery
import com.hedvig.android.owldroid.graphql.PayinStatusQuery
import com.hedvig.android.owldroid.type.NorwegianHomeContentLineOfBusiness
import com.hedvig.android.owldroid.type.PayinMethodStatus
import com.hedvig.android.owldroid.type.SwedishApartmentLineOfBusiness
import com.hedvig.android.owldroid.type.TypeOfContract
import com.hedvig.app.DevelopmentScreenAdapter.ViewHolder.Header.Companion.DEVELOPMENT_PREFERENCES
import com.hedvig.app.feature.dashboard.ui.DashboardViewModel
import java.time.LocalDate

class MockDashboardViewModel(context: Context) : DashboardViewModel() {
    override val data = MutableLiveData<Pair<DashboardQuery.Data?, PayinStatusQuery.Data?>>()

    init {
        val activePersona = context
            .getSharedPreferences(DEVELOPMENT_PREFERENCES, Context.MODE_PRIVATE)
            .getInt("mockPersona", 0)
        data.postValue(
            Pair(
                when (activePersona) {
                    0 -> SWEDISH_APARTMENT
                    1 -> SWEDISH_HOUSE
                    2 -> NORWEGIAN_HOME_CONTENTS
                    3 -> NORWEGIAN_TRAVEL
                    4 -> NORWEGIAN_HOME_CONTENTS_AND_TRAVEL
                    else -> SWEDISH_APARTMENT
                }, PayinStatusQuery.Data(PayinMethodStatus.NEEDS_SETUP)
            )
        )
    }

    companion object {
        private val SWEDISH_HOUSE_CONTRACT = DashboardQuery.Contract(
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
            typeOfContract = TypeOfContract.SE_HOUSE,
            upcomingRenewal = null,
            currentAgreement = DashboardQuery.CurrentAgreement(
                asAgreementCore = DashboardQuery.AsAgreementCore(
                    certificateUrl = "https://www.example.com"
                ),
                asSwedishHouseAgreement = DashboardQuery.AsSwedishHouseAgreement(
                    address = DashboardQuery.Address1(
                        fragments = DashboardQuery.Address1.Fragments(
                            AddressFragment(
                                street = "Testvägen 1",
                                postalCode = "123 45",
                                city = "Tensta"
                            )
                        )
                    ),
                    numberCoInsured = 2,
                    squareMeters = 50
                ),
                asNorwegianHomeContentAgreement = null,
                asNorwegianTravelAgreement = null,
                asSwedishApartmentAgreement = null
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
        )
        private val SWEDISH_APARTMENT_CONTRACT = DashboardQuery.Contract(
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
            upcomingRenewal = DashboardQuery.UpcomingRenewal(renewalDate = LocalDate.now(),
                draftCertificateUrl = "https://www.example.com"),
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
        )
        private val NORWEGIAN_HOME_CONTENTS_CONTRACT = DashboardQuery.Contract(
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
            displayName = "Innboforsikring",
            typeOfContract = TypeOfContract.NO_HOME_CONTENT_RENT,
            upcomingRenewal = DashboardQuery.UpcomingRenewal(
                renewalDate = LocalDate.of(2020, 6, 1),
                draftCertificateUrl = "https://www.example.com"
            ),
            currentAgreement = DashboardQuery.CurrentAgreement(
                asAgreementCore = DashboardQuery.AsAgreementCore(
                    certificateUrl = "https://www.example.com"
                ),
                asNorwegianHomeContentAgreement = DashboardQuery.AsNorwegianHomeContentAgreement(
                    address = DashboardQuery.Address2(
                        fragments = DashboardQuery.Address2.Fragments(
                            AddressFragment(
                                street = "Testvägen 1",
                                postalCode = "123 45",
                                city = "Tensta"
                            )
                        )
                    ),
                    numberCoInsured = 2,
                    squareMeters = 50,
                    nhcType = NorwegianHomeContentLineOfBusiness.RENT
                ),
                asSwedishApartmentAgreement = null,
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
        )

        private val NORWEGIAN_TRAVEL_CONTRACT = DashboardQuery.Contract(
            id = "eaaf8b5c-5a61-44a9-91bc-3de5b6bf878e",
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
            displayName = "Reiseforsikring",
            typeOfContract = TypeOfContract.NO_TRAVEL,
            upcomingRenewal = null,
            currentAgreement = DashboardQuery.CurrentAgreement(
                asAgreementCore = DashboardQuery.AsAgreementCore(
                    certificateUrl = "https://www.example.com"
                ),
                asNorwegianHomeContentAgreement = null,
                asNorwegianTravelAgreement = DashboardQuery.AsNorwegianTravelAgreement(
                    numberCoInsured = 2
                ),
                asSwedishApartmentAgreement = null,
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
        )

        val SWEDISH_APARTMENT = DashboardQuery.Data(
            listOf(SWEDISH_APARTMENT_CONTRACT)
        )

        val SWEDISH_HOUSE = DashboardQuery.Data(
            listOf(SWEDISH_HOUSE_CONTRACT)
        )

        val NORWEGIAN_HOME_CONTENTS = DashboardQuery.Data(
            listOf(NORWEGIAN_HOME_CONTENTS_CONTRACT)
        )

        val NORWEGIAN_TRAVEL = DashboardQuery.Data(
            listOf(NORWEGIAN_TRAVEL_CONTRACT)
        )

        val NORWEGIAN_HOME_CONTENTS_AND_TRAVEL = DashboardQuery.Data(
            listOf(NORWEGIAN_HOME_CONTENTS_CONTRACT, NORWEGIAN_TRAVEL_CONTRACT)
        )
    }
}
