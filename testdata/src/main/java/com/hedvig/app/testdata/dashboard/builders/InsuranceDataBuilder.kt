package com.hedvig.app.testdata.dashboard.builders

import com.hedvig.android.owldroid.graphql.InsuranceQuery
import com.hedvig.android.owldroid.graphql.fragment.ContractStatusFragment
import com.hedvig.android.owldroid.graphql.fragment.IconVariantsFragment
import com.hedvig.android.owldroid.graphql.fragment.InsurableLimitsFragment
import com.hedvig.android.owldroid.graphql.fragment.TableFragment
import com.hedvig.android.owldroid.graphql.fragment.UpcomingAgreementChangeFragment
import com.hedvig.android.owldroid.graphql.fragment.UpcomingAgreementFragment
import com.hedvig.android.owldroid.graphql.type.ActiveStatus
import com.hedvig.android.owldroid.graphql.type.AgreementCore
import com.hedvig.android.owldroid.graphql.type.AgreementStatus
import com.hedvig.android.owldroid.graphql.type.Contract
import com.hedvig.android.owldroid.graphql.type.IconVariants
import com.hedvig.android.owldroid.graphql.type.InsurableLimit
import com.hedvig.android.owldroid.graphql.type.SwedishApartmentAgreement
import com.hedvig.android.owldroid.graphql.type.Table
import com.hedvig.android.owldroid.graphql.type.TypeOfContractGradientOption
import com.hedvig.app.testdata.common.ContractStatus
import com.hedvig.app.testdata.common.builders.TableFragmentBuilder
import com.hedvig.app.testdata.feature.insurance.builders.PerilBuilder
import java.time.LocalDate

class InsuranceDataBuilder(
    private val contracts: List<ContractStatus> = emptyList(),
    private val renewal: InsuranceQuery.UpcomingRenewal? =
        InsuranceQuery.UpcomingRenewal(
            renewalDate = LocalDate.now(),
            draftCertificateUrl = "https://www.example.com"
        ),
    private val displayName: String = "Hemförsäkring",
    private val showUpcomingAgreement: Boolean = false,
    private val upcomingDetailsTable: TableFragment = TableFragmentBuilder().build(),
    private val crossSells: List<InsuranceQuery.PotentialCrossSell> = emptyList(),
    private val detailsTable: TableFragment = DEFAULT_DETAILS_TABLE,
    private val supportsAddressChange: Boolean = true,
) {

    fun build() = InsuranceQuery.Data(
        contracts = contracts.map { c ->
            InsuranceQuery.Contract(
                __typename = Contract.type.name,
                id = "120e9ac9-84b1-4e5d-add1-70a9bad340be",
                status = InsuranceQuery.Status(
                    __typename = c.typename,
                    fragments = InsuranceQuery.Status.Fragments(
                        contractStatusFragment = ContractStatusFragment(
                            __typename = c.typename,
                            asPendingStatus = if (c == ContractStatus.PENDING) {
                                ContractStatusFragment.AsPendingStatus(
                                    __typename = c.typename,
                                    pendingSince = null
                                )
                            } else {
                                null
                            },
                            asActiveInFutureStatus = when (c) {
                                ContractStatus.ACTIVE_IN_FUTURE -> ContractStatusFragment.AsActiveInFutureStatus(
                                    __typename = c.typename,
                                    futureInception = LocalDate.of(2025, 1, 1)
                                )
                                ContractStatus.ACTIVE_IN_FUTURE_INVALID ->
                                    ContractStatusFragment.AsActiveInFutureStatus(
                                        __typename = c.typename,
                                        futureInception = null
                                    )
                                else -> null
                            },
                            asActiveStatus = if (c == ContractStatus.ACTIVE) {
                                ContractStatusFragment.AsActiveStatus(
                                    __typename = c.typename,
                                    pastInception = LocalDate.now(),
                                    upcomingAgreementChange = ContractStatusFragment.UpcomingAgreementChange(
                                        newAgreement = ContractStatusFragment.NewAgreement(
                                            __typename = SwedishApartmentAgreement.type.name,
                                            asSwedishApartmentAgreement = ContractStatusFragment
                                                .AsSwedishApartmentAgreement(
                                                    __typename = SwedishApartmentAgreement.type.name,
                                                    activeFrom = LocalDate.of(2021, 4, 6)
                                                )
                                        )
                                    )
                                )
                            } else {
                                null
                            },
                            asActiveInFutureAndTerminatedInFutureStatus = if (
                                c == ContractStatus.ACTIVE_IN_FUTURE_AND_TERMINATED_IN_FUTURE
                            ) {
                                ContractStatusFragment.AsActiveInFutureAndTerminatedInFutureStatus(
                                    __typename = c.typename,
                                    futureInception = LocalDate.of(2024, 1, 1),
                                    futureTermination = LocalDate.of(2034, 1, 1)
                                )
                            } else {
                                null
                            },
                            asTerminatedInFutureStatus = null,
                            asTerminatedTodayStatus = if (c == ContractStatus.TERMINATED_TODAY) {
                                ContractStatusFragment.AsTerminatedTodayStatus(
                                    __typename = c.typename,
                                    today = LocalDate.now()
                                )
                            } else {
                                null
                            },
                            asTerminatedStatus = if (c == ContractStatus.TERMINATED) {
                                ContractStatusFragment.AsTerminatedStatus(
                                    __typename = c.typename,
                                    termination = null
                                )
                            } else {
                                null
                            }
                        )
                    )
                ),
                displayName = displayName,
                upcomingRenewal = renewal,
                currentAgreement = InsuranceQuery.CurrentAgreement(
                    __typename = AgreementCore.type.name,
                    asAgreementCore = InsuranceQuery.AsAgreementCore(
                        __typename = AgreementCore.type.name,
                        certificateUrl = "https://www.example.com",
                        status = AgreementStatus.ACTIVE,
                    ),
                ),
                currentAgreementDetailsTable = InsuranceQuery.CurrentAgreementDetailsTable(
                    __typename = Table.type.name,
                    fragments = InsuranceQuery.CurrentAgreementDetailsTable.Fragments(detailsTable),
                ),
                contractPerils = PerilBuilder().insuranceQueryBuild(5),
                insurableLimits = listOf(
                    InsuranceQuery.InsurableLimit(
                        __typename = InsurableLimit.type.name,
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
                            __typename = ActiveStatus.type.name,
                            asActiveStatus = if (c == ContractStatus.ACTIVE) {
                                UpcomingAgreementFragment.AsActiveStatus(
                                    __typename = c.typename,
                                    upcomingAgreementChange = if (showUpcomingAgreement) {
                                        UpcomingAgreementFragment.UpcomingAgreementChange(
                                            __typename = AgreementCore.type.name,
                                            fragments = UpcomingAgreementFragment.UpcomingAgreementChange.Fragments(
                                                upcomingAgreementChangeFragment = UpcomingAgreementChangeFragment(
                                                    newAgreement = UpcomingAgreementChangeFragment.NewAgreement(
                                                        __typename = AgreementCore.type.name,
                                                        asAgreementCore = UpcomingAgreementChangeFragment
                                                            .AsAgreementCore(
                                                                __typename = AgreementCore.type.name,
                                                                activeFrom = LocalDate.of(2021, 1, 13),
                                                            ),
                                                    )
                                                )
                                            )
                                        )
                                    } else {
                                        null
                                    }
                                )
                            } else {
                                null
                            },
                            asTerminatedInFutureStatus = null,
                            asTerminatedTodayStatus = null
                        ),
                        upcomingAgreementDetailsTable = UpcomingAgreementFragment.UpcomingAgreementDetailsTable(
                            __typename = "",
                            fragments = UpcomingAgreementFragment.UpcomingAgreementDetailsTable.Fragments(
                                upcomingDetailsTable
                            )
                        )
                    )
                ),
                logo = InsuranceQuery.Logo(
                    variants = InsuranceQuery.Variants(
                        __typename = IconVariants.type.name,
                        fragments = InsuranceQuery.Variants.Fragments(
                            IconVariantsFragment(
                                dark = IconVariantsFragment.Dark(svgUrl = "https://www.example.com"),
                                light = IconVariantsFragment.Light(svgUrl = "https://www.example.com")
                            )
                        )
                    )
                ),
            )
        },
        activeContractBundles = if (crossSells.isNotEmpty()) {
            listOf(
                InsuranceQuery.ActiveContractBundle(
                    potentialCrossSells = crossSells,
                )
            )
        } else {
            emptyList()
        }
    )

    companion object {
        private val DEFAULT_DETAILS_TABLE = TableFragmentBuilder(
            title = "",
            sections = listOf(
                "Home details" to listOf(
                    Triple("Adress", null, "Testvägen 1"),
                    Triple("Postal code", null, "123 45"),
                    Triple("Housing type", null, "Rental"),
                    Triple("Size", null, "50 m2")
                ),
                "Coinsured" to listOf(
                    Triple("Insured people", null, "You + 1 person"),
                )
            )
        ).build()
    }
}
