package com.hedvig.app

import android.content.Context
import com.hedvig.android.owldroid.fragment.AddressFragment
import com.hedvig.android.owldroid.fragment.ContractStatusFragment
import com.hedvig.android.owldroid.fragment.InsurableLimitsFragment
import com.hedvig.android.owldroid.fragment.PerilFragment
import com.hedvig.android.owldroid.graphql.InsuranceQuery
import com.hedvig.android.owldroid.type.NorwegianHomeContentLineOfBusiness
import com.hedvig.android.owldroid.type.SwedishApartmentLineOfBusiness
import com.hedvig.android.owldroid.type.TypeOfContract
import com.hedvig.app.DevelopmentScreenAdapter.ViewHolder.Header.Companion.DEVELOPMENT_PREFERENCES
import com.hedvig.app.feature.insurance.ui.InsuranceViewModel
import java.time.LocalDate

class MockInsuranceViewModel(context: Context) : InsuranceViewModel() {
    override fun load() {}

    init {
        val activePersona = context
            .getSharedPreferences(DEVELOPMENT_PREFERENCES, Context.MODE_PRIVATE)
            .getInt("mockPersona", 0)
        data.postValue(
            when (activePersona) {
                0 -> Result.success(SWEDISH_APARTMENT)
                1 -> Result.success(SWEDISH_HOUSE)
                2 -> Result.success(NORWEGIAN_HOME_CONTENTS)
                3 -> Result.success(NORWEGIAN_TRAVEL)
                4 -> Result.success(NORWEGIAN_HOME_CONTENTS_AND_TRAVEL)
                else -> Result.success(SWEDISH_APARTMENT)
            }
        )
    }

    companion object {
        private val SWEDISH_HOUSE_CONTRACT = InsuranceQuery.Contract(
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
            typeOfContract = TypeOfContract.SE_HOUSE,
            upcomingRenewal = null,
            currentAgreement = InsuranceQuery.CurrentAgreement(
                asAgreementCore = InsuranceQuery.AsAgreementCore(
                    certificateUrl = "https://www.example.com"
                ),
                asSwedishHouseAgreement = InsuranceQuery.AsSwedishHouseAgreement(
                    address = InsuranceQuery.Address1(
                        fragments = InsuranceQuery.Address1.Fragments(
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
                asSwedishApartmentAgreement = null,
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
        private val SWEDISH_APARTMENT_CONTRACT = InsuranceQuery.Contract(
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
            typeOfContract = TypeOfContract.SE_APARTMENT_RENT,
            upcomingRenewal = InsuranceQuery.UpcomingRenewal(renewalDate = LocalDate.now(),
                draftCertificateUrl = "https://www.example.com"),
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
        private val NORWEGIAN_HOME_CONTENTS_CONTRACT = InsuranceQuery.Contract(
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
            displayName = "Innboforsikring",
            typeOfContract = TypeOfContract.NO_HOME_CONTENT_RENT,
            upcomingRenewal = InsuranceQuery.UpcomingRenewal(
                renewalDate = LocalDate.of(2020, 6, 1),
                draftCertificateUrl = "https://www.example.com"
            ),
            currentAgreement = InsuranceQuery.CurrentAgreement(
                asAgreementCore = InsuranceQuery.AsAgreementCore(
                    certificateUrl = "https://www.example.com"
                ),
                asNorwegianHomeContentAgreement = InsuranceQuery.AsNorwegianHomeContentAgreement(
                    address = InsuranceQuery.Address2(
                        fragments = InsuranceQuery.Address2.Fragments(
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

        private val NORWEGIAN_TRAVEL_CONTRACT = InsuranceQuery.Contract(
            id = "eaaf8b5c-5a61-44a9-91bc-3de5b6bf878e",
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
            displayName = "Reiseforsikring",
            typeOfContract = TypeOfContract.NO_TRAVEL,
            upcomingRenewal = null,
            currentAgreement = InsuranceQuery.CurrentAgreement(
                asAgreementCore = InsuranceQuery.AsAgreementCore(
                    certificateUrl = "https://www.example.com"
                ),
                asNorwegianHomeContentAgreement = null,
                asNorwegianTravelAgreement = InsuranceQuery.AsNorwegianTravelAgreement(
                    numberCoInsured = 2
                ),
                asSwedishApartmentAgreement = null,
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

        val SWEDISH_APARTMENT = InsuranceQuery.Data(
            listOf(SWEDISH_APARTMENT_CONTRACT)
        )

        val SWEDISH_HOUSE = InsuranceQuery.Data(
            listOf(SWEDISH_HOUSE_CONTRACT)
        )

        val NORWEGIAN_HOME_CONTENTS = InsuranceQuery.Data(
            listOf(NORWEGIAN_HOME_CONTENTS_CONTRACT)
        )

        val NORWEGIAN_TRAVEL = InsuranceQuery.Data(
            listOf(NORWEGIAN_TRAVEL_CONTRACT)
        )

        val NORWEGIAN_HOME_CONTENTS_AND_TRAVEL = InsuranceQuery.Data(
            listOf(NORWEGIAN_HOME_CONTENTS_CONTRACT, NORWEGIAN_TRAVEL_CONTRACT)
        )
    }
}
