package com.hedvig.app

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.hedvig.android.owldroid.fragment.AddressFragment
import com.hedvig.android.owldroid.graphql.DashboardQuery
import com.hedvig.android.owldroid.graphql.DirectDebitQuery
import com.hedvig.android.owldroid.type.AgreementStatus
import com.hedvig.android.owldroid.type.ContractStatus
import com.hedvig.android.owldroid.type.DirectDebitStatus
import com.hedvig.android.owldroid.type.NorwegianHomeContentLineOfBusiness
import com.hedvig.android.owldroid.type.SwedishApartmentLineOfBusiness
import com.hedvig.app.feature.dashboard.ui.DashboardViewModel
import org.threeten.bp.LocalDate

class MockDashboardViewModel(context: Context) : DashboardViewModel() {
    override val data = MutableLiveData<DashboardQuery.Data>()
    override val directDebitStatus = MutableLiveData<DirectDebitQuery.Data>()

    init {
        val activePersona = context
            .getSharedPreferences(DevelopmentActivity.DEVELOPMENT_PREFERENCES, Context.MODE_PRIVATE)
            .getInt("mockPersona", 0)
        data.postValue(when (activePersona) {
            0 -> SWEDISH_APARTMENT
            1 -> SWEDISH_HOUSE
            2 -> NORWEGIAN_HOME_CONTENTS
            3 -> NORWEGIAN_TRAVEL
            4 -> NORWEGIAN_HOME_CONTENTS_AND_TRAVEL
            else -> SWEDISH_APARTMENT
        })

        directDebitStatus.postValue(DirectDebitQuery.Data(DirectDebitStatus.NEEDS_SETUP))
    }

    companion object {
        private val SWEDISH_HOUSE_CONTRACT = DashboardQuery.Contract(
            id = "120e9ac9-84b1-4e5d-add1-70a9bad340be",
            status = ContractStatus.ACTIVE,
            inception = LocalDate.of(2020, 1, 1),
            displayName = "Hemförsäkring",
            upcomingRenewal = null,
            currentAgreement = DashboardQuery.CurrentAgreement(
                asAgreementCore = DashboardQuery.AsAgreementCore(
                    status = AgreementStatus.ACTIVE,
                    activeFrom = LocalDate.of(2020, 1, 1),
                    activeTo = null
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
                    title = "Mock",
                    description = "Mock",
                    icon = DashboardQuery.Icon(
                        variants = DashboardQuery.Variants(
                            dark = DashboardQuery.Dark(
                                svgUrl = "/app-content-service/fire_dark.svg"
                            ),
                            light = DashboardQuery.Light(
                                svgUrl = "/app-content-service/fire.svg"
                            )
                        )
                    )
                )
            )
        )
        private val SWEDISH_APARTMENT_CONTRACT = DashboardQuery.Contract(
            id = "120e9ac9-84b1-4e5d-add1-70a9bad340be",
            status = ContractStatus.ACTIVE,
            inception = LocalDate.of(2020, 1, 1),
            displayName = "Hemförsäkring",
            upcomingRenewal = null,
            currentAgreement = DashboardQuery.CurrentAgreement(
                asAgreementCore = DashboardQuery.AsAgreementCore(
                    status = AgreementStatus.ACTIVE,
                    activeFrom = LocalDate.of(2020, 1, 1),
                    activeTo = null
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
                    title = "Mock",
                    description = "Mock",
                    icon = DashboardQuery.Icon(
                        variants = DashboardQuery.Variants(
                            dark = DashboardQuery.Dark(
                                svgUrl = "/app-content-service/fire_dark.svg"
                            ),
                            light = DashboardQuery.Light(
                                svgUrl = "/app-content-service/fire.svg"
                            )
                        )
                    )
                ),
                DashboardQuery.Peril(
                    title = "Mock",
                    description = "Mock",
                    icon = DashboardQuery.Icon(
                        variants = DashboardQuery.Variants(
                            dark = DashboardQuery.Dark(
                                svgUrl = "/app-content-service/fire_dark.svg"
                            ),
                            light = DashboardQuery.Light(
                                svgUrl = "/app-content-service/fire.svg"
                            )
                        )
                    )
                ),
                DashboardQuery.Peril(
                    title = "Mock",
                    description = "Mock",
                    icon = DashboardQuery.Icon(
                        variants = DashboardQuery.Variants(
                            dark = DashboardQuery.Dark(
                                svgUrl = "/app-content-service/fire_dark.svg"
                            ),
                            light = DashboardQuery.Light(
                                svgUrl = "/app-content-service/fire.svg"
                            )
                        )
                    )
                ),
                DashboardQuery.Peril(
                    title = "Mock",
                    description = "Mock",
                    icon = DashboardQuery.Icon(
                        variants = DashboardQuery.Variants(
                            dark = DashboardQuery.Dark(
                                svgUrl = "/app-content-service/fire_dark.svg"
                            ),
                            light = DashboardQuery.Light(
                                svgUrl = "/app-content-service/fire.svg"
                            )
                        )
                    )
                ),
                DashboardQuery.Peril(
                    title = "Mock",
                    description = "Mock",
                    icon = DashboardQuery.Icon(
                        variants = DashboardQuery.Variants(
                            dark = DashboardQuery.Dark(
                                svgUrl = "/app-content-service/fire_dark.svg"
                            ),
                            light = DashboardQuery.Light(
                                svgUrl = "/app-content-service/fire.svg"
                            )
                        )
                    )
                )
            )
        )
        private val NORWEGIAN_HOME_CONTENTS_CONTRACT = DashboardQuery.Contract(
            id = "120e9ac9-84b1-4e5d-add1-70a9bad340be",
            status = ContractStatus.ACTIVE,
            inception = LocalDate.of(2020, 1, 1),
            displayName = "Innboforsikring",
            upcomingRenewal = null,
            currentAgreement = DashboardQuery.CurrentAgreement(
                asAgreementCore = DashboardQuery.AsAgreementCore(
                    status = AgreementStatus.ACTIVE,
                    activeFrom = LocalDate.of(2020, 1, 1),
                    activeTo = null
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
                    title = "Mock",
                    description = "Mock",
                    icon = DashboardQuery.Icon(
                        variants = DashboardQuery.Variants(
                            dark = DashboardQuery.Dark(
                                svgUrl = "/app-content-service/fire_dark.svg"
                            ),
                            light = DashboardQuery.Light(
                                svgUrl = "/app-content-service/fire.svg"
                            )
                        )
                    )
                )
            )
        )

        private val NORWEGIAN_TRAVEL_CONTRACT = DashboardQuery.Contract(
            id = "eaaf8b5c-5a61-44a9-91bc-3de5b6bf878e",
            status = ContractStatus.ACTIVE,
            inception = LocalDate.of(2020, 1, 1),
            displayName = "Reiseforsikring",
            upcomingRenewal = null,
            currentAgreement = DashboardQuery.CurrentAgreement(
                asAgreementCore = DashboardQuery.AsAgreementCore(
                    status = AgreementStatus.ACTIVE,
                    activeFrom = LocalDate.of(2020, 1, 1),
                    activeTo = null
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
                    title = "Mock",
                    description = "Mock",
                    icon = DashboardQuery.Icon(
                        variants = DashboardQuery.Variants(
                            dark = DashboardQuery.Dark(
                                svgUrl = "/app-content-service/fire_dark.svg"
                            ),
                            light = DashboardQuery.Light(
                                svgUrl = "/app-content-service/fire.svg"
                            )
                        )
                    )
                )
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
