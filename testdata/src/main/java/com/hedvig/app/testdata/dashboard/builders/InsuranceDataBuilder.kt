package com.hedvig.app.testdata.dashboard.builders

import com.hedvig.android.owldroid.fragment.AddressFragment
import com.hedvig.android.owldroid.fragment.ContractStatusFragment
import com.hedvig.android.owldroid.fragment.InsurableLimitsFragment
import com.hedvig.android.owldroid.graphql.InsuranceQuery
import com.hedvig.android.owldroid.type.AgreementStatus
import com.hedvig.android.owldroid.type.DanishHomeContentLineOfBusiness
import com.hedvig.android.owldroid.type.SwedishApartmentLineOfBusiness
import com.hedvig.android.owldroid.type.TypeOfContract
import com.hedvig.app.testdata.common.ContractStatus
import com.hedvig.app.testdata.feature.insurance.builders.PerilBuilder
import java.time.LocalDate

class InsuranceDataBuilder(
    private val contracts: List<ContractStatus> = emptyList(),
    private val typeOfContract: TypeOfContract = TypeOfContract.SE_APARTMENT_BRF,
    private val renewal: InsuranceQuery.UpcomingRenewal? =
        InsuranceQuery.UpcomingRenewal(
            renewalDate = LocalDate.now(),
            draftCertificateUrl = "https://www.example.com"
        ),
    private val displayName: String = "Hemförsäkring",
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
                                ContractStatus.ACTIVE_IN_FUTURE_INVALID ->
                                    ContractStatusFragment.AsActiveInFutureStatus(
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
                            asActiveInFutureAndTerminatedInFutureStatus = if (
                                c == ContractStatus.ACTIVE_IN_FUTURE_AND_TERMINATED_IN_FUTURE
                            ) {
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
                        certificateUrl = "https://www.example.com",
                        status = AgreementStatus.ACTIVE,
                    ),
                    asSwedishApartmentAgreement = when (typeOfContract) {
                        TypeOfContract.SE_APARTMENT_BRF,
                        TypeOfContract.SE_APARTMENT_RENT,
                        TypeOfContract.SE_APARTMENT_STUDENT_BRF,
                        TypeOfContract.SE_APARTMENT_STUDENT_RENT,
                        -> InsuranceQuery.AsSwedishApartmentAgreement(
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
                            saType = when (typeOfContract) {
                                TypeOfContract.SE_APARTMENT_BRF -> SwedishApartmentLineOfBusiness.BRF
                                TypeOfContract.SE_APARTMENT_STUDENT_BRF -> SwedishApartmentLineOfBusiness.STUDENT_BRF
                                TypeOfContract.SE_APARTMENT_RENT -> SwedishApartmentLineOfBusiness.RENT
                                TypeOfContract.SE_APARTMENT_STUDENT_RENT -> SwedishApartmentLineOfBusiness.STUDENT_RENT
                                else -> throw Error("Unreachable")
                            }
                        )
                        else -> null
                    },
                    asNorwegianHomeContentAgreement = null,
                    asNorwegianTravelAgreement = null,
                    asSwedishHouseAgreement = null,
                    asDanishHomeContentAgreement = when (typeOfContract) {
                        TypeOfContract.DK_HOME_CONTENT_OWN,
                        TypeOfContract.DK_HOME_CONTENT_RENT,
                        TypeOfContract.DK_HOME_CONTENT_STUDENT_OWN,
                        TypeOfContract.DK_HOME_CONTENT_STUDENT_RENT,
                        -> InsuranceQuery.AsDanishHomeContentAgreement(
                            address = InsuranceQuery.Address3(
                                fragments = InsuranceQuery.Address3.Fragments(
                                    AddressFragment(
                                        street = "Testvägen 1",
                                        postalCode = "123 45",
                                        city = "Tensta"
                                    )
                                )
                            ),
                            numberCoInsured = 2,
                            squareMeters = 50,
                            dhcType = when (typeOfContract) {
                                TypeOfContract.DK_HOME_CONTENT_OWN -> DanishHomeContentLineOfBusiness.OWN
                                TypeOfContract.DK_HOME_CONTENT_RENT -> DanishHomeContentLineOfBusiness.RENT
                                TypeOfContract.DK_HOME_CONTENT_STUDENT_OWN ->
                                    DanishHomeContentLineOfBusiness.STUDENT_OWN
                                TypeOfContract.DK_HOME_CONTENT_STUDENT_RENT ->
                                    DanishHomeContentLineOfBusiness.STUDENT_RENT
                                else -> throw Error("Unreachable")
                            }
                        )
                        else -> null
                    },
                    asDanishTravelAgreement = when (typeOfContract) {
                        TypeOfContract.DK_TRAVEL,
                        TypeOfContract.DK_TRAVEL_STUDENT,
                        -> InsuranceQuery.AsDanishTravelAgreement(
                            numberCoInsured = 2
                        )
                        else -> null
                    },
                    asDanishAccidentAgreement = when (typeOfContract) {
                        TypeOfContract.DK_ACCIDENT,
                        TypeOfContract.DK_ACCIDENT_STUDENT,
                        -> InsuranceQuery.AsDanishAccidentAgreement(
                            numberCoInsured = 2
                        )
                        else -> null
                    }
                ),
                perils = PerilBuilder().insuranceQueryBuild(5),
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
