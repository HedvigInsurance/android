package com.hedvig.app.testdata.feature.insurance.builders

import com.hedvig.android.owldroid.graphql.InsuranceQuery
import com.hedvig.android.owldroid.graphql.fragment.ContractStatusFragment
import com.hedvig.android.owldroid.graphql.fragment.IconVariantsFragment
import com.hedvig.android.owldroid.graphql.fragment.InsurableLimitsFragment
import com.hedvig.android.owldroid.graphql.fragment.TableFragment
import com.hedvig.android.owldroid.graphql.fragment.UpcomingAgreementFragment
import com.hedvig.android.owldroid.graphql.type.AgreementStatus
import com.hedvig.android.owldroid.graphql.type.NorwegianTravelAgreement
import com.hedvig.android.owldroid.graphql.type.SwedishApartmentAgreement
import com.hedvig.android.owldroid.graphql.type.TypeOfContractGradientOption
import com.hedvig.app.testdata.common.builders.TableFragmentBuilder
import java.time.LocalDate

class InsuranceContractBuilder(
    private val renewal: InsuranceQuery.UpcomingRenewal? =
        InsuranceQuery.UpcomingRenewal(
            renewalDate = LocalDate.now(),
            draftCertificateUrl = "https://www.example.com"
        ),
    private val agreementStatus: AgreementStatus = AgreementStatus.ACTIVE,
    private val showUpcomingAgreement: Boolean = false,
    private val detailsTable: TableFragment = TableFragmentBuilder().build(),
    private val supportsAddressChange: Boolean = true,
) {

    fun build() = InsuranceQuery.Contract(
        __typename = "",
        id = "120e9ac9-84b1-4e5d-add1-70a9bad340be",
        status = InsuranceQuery.Status(
            __typename = "",
            fragments = InsuranceQuery.Status.Fragments(
                contractStatusFragment = ContractStatusFragment(
                    __typename = "",
                    asPendingStatus = null,
                    asActiveInFutureStatus = null,
                    asActiveStatus = ContractStatusFragment.AsActiveStatus(
                        __typename = "",
                        pastInception = LocalDate.of(2020, 2, 1),
                        upcomingAgreementChange = if (showUpcomingAgreement) {
                            ContractStatusFragment.UpcomingAgreementChange(
                                newAgreement = ContractStatusFragment.NewAgreement(
                                    __typename = SwedishApartmentAgreement.type.name,
                                    asSwedishApartmentAgreement = ContractStatusFragment.AsSwedishApartmentAgreement(
                                        __typename = SwedishApartmentAgreement.type.name,
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
        upcomingRenewal = renewal,
        currentAgreement = InsuranceQuery.CurrentAgreement(
            __typename = NorwegianTravelAgreement.type.name,
            asAgreementCore = InsuranceQuery.AsAgreementCore(
                __typename = NorwegianTravelAgreement.type.name,
                certificateUrl = "https://www.example.com",
                status = agreementStatus,
            ),
        ),
        currentAgreementDetailsTable = InsuranceQuery.CurrentAgreementDetailsTable(
            __typename = "",
            fragments = InsuranceQuery.CurrentAgreementDetailsTable.Fragments(detailsTable),
        ),
        contractPerils = PerilBuilder().insuranceQueryBuild(5),
        insurableLimits = listOf(
            InsuranceQuery.InsurableLimit(
                __typename = "",
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
        supportsAddressChange = supportsAddressChange,
        fragments = InsuranceQuery.Contract.Fragments(
            upcomingAgreementFragment = UpcomingAgreementFragment(
                status = UpcomingAgreementFragment.Status(
                    __typename = "",
                    asActiveStatus = null,
                    asTerminatedTodayStatus = null,
                    asTerminatedInFutureStatus = null
                ),
                upcomingAgreementDetailsTable = UpcomingAgreementFragment.UpcomingAgreementDetailsTable(
                    __typename = "",
                    fragments = UpcomingAgreementFragment.UpcomingAgreementDetailsTable.Fragments(
                        TableFragmentBuilder().build()
                    )
                )
            )
        ),
        logo = InsuranceQuery.Logo(
            variants = InsuranceQuery.Variants(
                __typename = "",
                fragments = InsuranceQuery.Variants.Fragments(
                    IconVariantsFragment(
                        dark = IconVariantsFragment.Dark(svgUrl = "https://www.example.com"),
                        light = IconVariantsFragment.Light(svgUrl = "https://www.example.com")
                    )
                )
            )
        )
    )
}
