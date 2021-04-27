package com.hedvig.app.testdata.feature.changeaddress

import com.hedvig.android.owldroid.fragment.AddressFragment
import com.hedvig.android.owldroid.fragment.UpcomingAgreementFragment
import com.hedvig.android.owldroid.graphql.SelfChangeEligibilityQuery
import com.hedvig.android.owldroid.graphql.UpcomingAgreementQuery
import com.hedvig.android.owldroid.type.SwedishApartmentLineOfBusiness
import com.hedvig.app.testdata.feature.changeaddress.builders.SelfChangeEligibilityBuilder
import com.hedvig.app.testdata.feature.changeaddress.builders.UpcomingAgreementBuilder
import java.time.LocalDate

val UPCOMING_AGREEMENT_NONE = UpcomingAgreementQuery.Data(
    contracts = listOf(
        UpcomingAgreementQuery.Contract(
            status = UpcomingAgreementQuery.Status(
                asActiveStatus = null,
                asTerminatedInFutureStatus = null,
                asTerminatedTodayStatus = null,
            )
        )
    )
)

val UPCOMING_AGREEMENT_SWEDISH_APARTMENT = UpcomingAgreementQuery.Data(
    contracts = listOf(
        UpcomingAgreementQuery.Contract(
            status = UpcomingAgreementQuery.Status(
                asActiveStatus = UpcomingAgreementQuery.AsActiveStatus(
                    upcomingAgreementChange = UpcomingAgreementBuilder(
                        UpcomingAgreementFragment.NewAgreement(
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
                                saType = SwedishApartmentLineOfBusiness.RENT,
                                numberCoInsured = 2
                            ),
                            asDanishHomeContentAgreement = null,
                            asNorwegianHomeContentAgreement = null,
                            asSwedishHouseAgreement = null
                        )
                    ).build()
                ),
                asTerminatedInFutureStatus = null,
                asTerminatedTodayStatus = null,
            )
        )
    )
)

val UPCOMING_AGREEMENT_SWEDISH_HOUSE = UpcomingAgreementQuery.Data(
    contracts = listOf(
        UpcomingAgreementQuery.Contract(
            status = UpcomingAgreementQuery.Status(
                asActiveStatus = UpcomingAgreementQuery.AsActiveStatus(
                    upcomingAgreementChange = UpcomingAgreementBuilder(
                        UpcomingAgreementFragment.NewAgreement(
                            asSwedishApartmentAgreement = null,
                            asDanishHomeContentAgreement = null,
                            asNorwegianHomeContentAgreement = null,
                            asSwedishHouseAgreement = UpcomingAgreementFragment.AsSwedishHouseAgreement(
                                address = UpcomingAgreementFragment.Address1(
                                    fragments = UpcomingAgreementFragment.Address1.Fragments(
                                        addressFragment = AddressFragment(
                                            street = "Test Street 123",
                                            postalCode = "123 TEST",
                                            city = "Test City"
                                        )
                                    )
                                ),
                                squareMeters = 50,
                                activeFrom = LocalDate.of(2021, 4, 11),
                                numberCoInsured = 2,
                                yearOfConstruction = 2002,
                                ancillaryArea = 34,
                                extraBuildings = listOf(
                                    UpcomingAgreementFragment.ExtraBuilding(
                                        asExtraBuildingCore = UpcomingAgreementFragment.AsExtraBuildingCore(
                                            area = 34,
                                            displayName = "Garage",
                                            hasWaterConnected = true
                                        )
                                    ),
                                    UpcomingAgreementFragment.ExtraBuilding(
                                        asExtraBuildingCore = UpcomingAgreementFragment.AsExtraBuildingCore(
                                            area = 14,
                                            displayName = "Attefall",
                                            hasWaterConnected = false
                                        )
                                    )
                                ),
                                isSubleted = false,
                                numberOfBathrooms = 2
                            )
                        )
                    ).build()
                ),
                asTerminatedInFutureStatus = null,
                asTerminatedTodayStatus = null,
            )
        )
    )
)


val SELF_CHANGE_ELIGIBILITY = SelfChangeEligibilityQuery.Data(
    SelfChangeEligibilityBuilder(
        embarkStoryId = "testId").build()
)

val BLOCKED_SELF_CHANGE_ELIGIBILITY = SelfChangeEligibilityQuery.Data(
    SelfChangeEligibilityBuilder().build()
)
