package com.hedvig.app.testdata.feature.insurance.builders

import com.hedvig.android.owldroid.fragment.ContractStatusFragment
import com.hedvig.android.owldroid.fragment.InsurableLimitsFragment
import com.hedvig.android.owldroid.fragment.TableFragment
import com.hedvig.android.owldroid.fragment.UpcomingAgreementFragment
import com.hedvig.android.owldroid.graphql.InsuranceQuery
import com.hedvig.android.owldroid.type.AgreementStatus
import com.hedvig.android.owldroid.type.TypeOfContract
import com.hedvig.android.owldroid.type.TypeOfContractGradientOption
import com.hedvig.app.testdata.common.builders.TableFragmentBuilder
import java.time.LocalDate

class InsuranceContractBuilder(
    private val type: TypeOfContract = TypeOfContract.SE_APARTMENT_RENT,
    private val renewal: InsuranceQuery.UpcomingRenewal? =
        InsuranceQuery.UpcomingRenewal(
            renewalDate = LocalDate.now(),
            draftCertificateUrl = "https://www.example.com"
        ),
    private val agreementStatus: AgreementStatus = AgreementStatus.ACTIVE,
    private val showUpcomingAgreement: Boolean = false,
    private val detailsTable: TableFragment = TableFragmentBuilder().build(),
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
                __typename = "NorwegianTravelAgreement",
                certificateUrl = "https://www.example.com",
                status = agreementStatus,
            ),
        ),
        currentAgreementDetailsTable = InsuranceQuery.CurrentAgreementDetailsTable(
            fragments = InsuranceQuery.CurrentAgreementDetailsTable.Fragments(detailsTable),
        ),
        contractPerils = PerilBuilder().insuranceQueryBuild(5),
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
        statusPills = emptyList(),
        detailPills = emptyList(),
        gradientOption = TypeOfContractGradientOption.GRADIENT_ONE,
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
