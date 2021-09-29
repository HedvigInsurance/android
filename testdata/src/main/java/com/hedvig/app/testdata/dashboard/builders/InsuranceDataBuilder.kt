package com.hedvig.app.testdata.dashboard.builders

import com.hedvig.android.owldroid.fragment.AddressFragment
import com.hedvig.android.owldroid.fragment.ContractStatusFragment
import com.hedvig.android.owldroid.fragment.InsurableLimitsFragment
import com.hedvig.android.owldroid.fragment.TableFragment
import com.hedvig.android.owldroid.fragment.UpcomingAgreementChangeFragment
import com.hedvig.android.owldroid.fragment.UpcomingAgreementFragment
import com.hedvig.android.owldroid.graphql.InsuranceQuery
import com.hedvig.android.owldroid.type.AgreementStatus
import com.hedvig.android.owldroid.type.TypeOfContractGradientOption
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
) {

    fun build() = InsuranceQuery.Data(
        contracts = contracts.map { c ->
            InsuranceQuery.Contract(
                id = "120e9ac9-84b1-4e5d-add1-70a9bad340be",
                status = InsuranceQuery.Status(
                    __typename = c.toTypename(),
                    fragments = InsuranceQuery.Status.Fragments(
                        contractStatusFragment = ContractStatusFragment(
                            __typename = c.toTypename(),
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
                                    pastInception = LocalDate.now(),
                                    upcomingAgreementChange = ContractStatusFragment.UpcomingAgreementChange(
                                        newAgreement = ContractStatusFragment.NewAgreement(
                                            asSwedishApartmentAgreement = ContractStatusFragment
                                                .AsSwedishApartmentAgreement(
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
                upcomingRenewal = renewal,
                currentAgreement = InsuranceQuery.CurrentAgreement(
                    asAgreementCore = InsuranceQuery.AsAgreementCore(
                        certificateUrl = "https://www.example.com",
                        status = AgreementStatus.ACTIVE,
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
                            __typename = c.toTypename(),
                            asActiveStatus = if (c == ContractStatus.ACTIVE) {
                                UpcomingAgreementFragment.AsActiveStatus(
                                    upcomingAgreementChange = if (showUpcomingAgreement) {
                                        UpcomingAgreementFragment.UpcomingAgreementChange(
                                            fragments = UpcomingAgreementFragment.UpcomingAgreementChange.Fragments(
                                                upcomingAgreementChangeFragment = UpcomingAgreementChangeFragment(
                                                    newAgreement = UpcomingAgreementChangeFragment.NewAgreement(
                                                        asSwedishApartmentAgreement = UpcomingAgreementChangeFragment
                                                            .AsSwedishApartmentAgreement(
                                                                address = UpcomingAgreementChangeFragment.Address(
                                                                    fragments = UpcomingAgreementChangeFragment.Address
                                                                        .Fragments(
                                                                            addressFragment = AddressFragment(
                                                                                street = "Test street",
                                                                                postalCode = "123",
                                                                                city = "Test city"
                                                                            )
                                                                        )
                                                                ),
                                                                activeFrom = LocalDate.of(2021, 1, 13)
                                                            ),
                                                        asDanishHomeContentAgreement = null,
                                                        asNorwegianHomeContentAgreement = null,
                                                        asSwedishHouseAgreement = null
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
                            fragments = UpcomingAgreementFragment.UpcomingAgreementDetailsTable.Fragments(
                                upcomingDetailsTable
                            )
                        )
                    )
                )
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
