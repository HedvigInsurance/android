package com.hedvig.app.testdata.feature.insurance.builders

import com.hedvig.android.owldroid.fragment.AddressFragment
import com.hedvig.android.owldroid.fragment.ContractStatusFragment
import com.hedvig.android.owldroid.fragment.InsurableLimitsFragment
import com.hedvig.android.owldroid.fragment.UpcomingAgreementFragment
import com.hedvig.android.owldroid.graphql.InsuranceQuery
import com.hedvig.android.owldroid.type.AgreementStatus
import com.hedvig.android.owldroid.type.DanishHomeContentLineOfBusiness
import com.hedvig.android.owldroid.type.NorwegianHomeContentLineOfBusiness
import com.hedvig.android.owldroid.type.SwedishApartmentLineOfBusiness
import com.hedvig.android.owldroid.type.TypeOfContract
import com.hedvig.app.testdata.common.builders.TableFragmentBuilder
import java.time.LocalDate

class InsuranceContractBuilder(
    private val type: TypeOfContract = TypeOfContract.SE_APARTMENT_RENT,
    private val coinsured: Int = 2,
    private val renewal: InsuranceQuery.UpcomingRenewal? =
        InsuranceQuery.UpcomingRenewal(
            renewalDate = LocalDate.now(),
            draftCertificateUrl = "https://www.example.com"
        ),
    private val agreementStatus: AgreementStatus = AgreementStatus.ACTIVE,
    private val showUpcomingAgreement: Boolean = false,
) {

    fun build() = InsuranceQuery.Contract(
        id = "120e9ac9-84b1-4e5d-add1-70a9bad340be",
        status = InsuranceQuery.Status(
            fragments = InsuranceQuery.Status.Fragments(
                contractStatusFragment = ContractStatusFragment(
                    asPendingStatus = null,
                    asActiveInFutureStatus = null,
                    asActiveStatus = ContractStatusFragment.AsActiveStatus(
                        pastInception = LocalDate.of(2020, 2, 1),
                        upcomingAgreementChange = if (showUpcomingAgreement) {
                            ContractStatusFragment.UpcomingAgreementChange(
                                newAgreement = ContractStatusFragment.NewAgreement(
                                    asSwedishApartmentAgreement = ContractStatusFragment.AsSwedishApartmentAgreement(
                                        activeFrom = LocalDate.of(2021, 4, 6)
                                    )
                                )
                            )
                        } else null
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
                certificateUrl = "https://www.example.com",
                status = agreementStatus,
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
            asDanishHomeContentAgreement = if (type == TypeOfContract.DK_HOME_CONTENT_OWN) {
                InsuranceQuery.AsDanishHomeContentAgreement(
                    address = InsuranceQuery.Address3(
                        fragments = InsuranceQuery.Address3.Fragments(
                            AddressFragment(
                                street = "Testvägen 1",
                                postalCode = "123 45",
                                city = "Tensta",
                            )
                        )
                    ),
                    numberCoInsured = coinsured,
                    squareMeters = 50,
                    dhcType = when (type) {
                        TypeOfContract.DK_HOME_CONTENT_OWN -> DanishHomeContentLineOfBusiness.OWN
                        TypeOfContract.DK_HOME_CONTENT_RENT -> DanishHomeContentLineOfBusiness.RENT
                        TypeOfContract.DK_HOME_CONTENT_STUDENT_OWN -> DanishHomeContentLineOfBusiness.STUDENT_OWN
                        TypeOfContract.DK_HOME_CONTENT_STUDENT_RENT -> DanishHomeContentLineOfBusiness.STUDENT_RENT
                        else -> throw Error("Unreachable")
                    }
                )
            } else {
                null
            },
            asDanishTravelAgreement = when (type) {
                TypeOfContract.DK_TRAVEL,
                TypeOfContract.DK_TRAVEL_STUDENT,
                -> InsuranceQuery.AsDanishTravelAgreement(
                    numberCoInsured = 2
                )
                else -> null
            },
            asDanishAccidentAgreement = when (type) {
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
        ),
        fragments = InsuranceQuery.Contract.Fragments(
            upcomingAgreementFragment = UpcomingAgreementFragment(
                status = UpcomingAgreementFragment.Status(
                    asActiveStatus = null,
                    asTerminatedTodayStatus = null,
                    asTerminatedInFutureStatus = null
                ),
                upcomingAgreementDetailsTable = UpcomingAgreementFragment.UpcomingAgreementDetailsTable(
                    fragments = UpcomingAgreementFragment.UpcomingAgreementDetailsTable.Fragments(
                        TableFragmentBuilder().build()
                    )
                )
            )
        )
    )
}
