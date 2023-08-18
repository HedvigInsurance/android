package com.hedvig.app.testdata.dashboard.builders

import com.hedvig.app.testdata.common.ContractStatus
import com.hedvig.app.testdata.common.builders.TableFragmentBuilder
import com.hedvig.app.testdata.feature.insurance.builders.PerilBuilder
import giraffe.InsuranceQuery
import giraffe.fragment.ContractStatusFragment
import giraffe.fragment.IconVariantsFragment
import giraffe.fragment.InsurableLimitsFragment
import giraffe.fragment.TableFragment
import giraffe.fragment.UpcomingAgreementChangeFragment
import giraffe.fragment.UpcomingAgreementFragment
import giraffe.type.AgreementStatus
import giraffe.type.Contract
import giraffe.type.IconVariants
import giraffe.type.InsurableLimit
import giraffe.type.SwedishApartmentAgreement
import giraffe.type.Table
import giraffe.type.TypeOfContract
import giraffe.type.UpcomingAgreementChange
import java.time.LocalDate

class InsuranceDataBuilder(
  private val contracts: List<ContractStatus> = emptyList(),
  private val renewal: InsuranceQuery.UpcomingRenewal? =
    InsuranceQuery.UpcomingRenewal(
      renewalDate = LocalDate.of(2021, 5, 6),
      draftCertificateUrl = "https://www.example.com",
    ),
  private val displayName: String = "Hemförsäkring",
  private val showUpcomingAgreement: Boolean = false,
  private val upcomingDetailsTable: TableFragment = TableFragmentBuilder().build(),
  private val detailsTable: TableFragment = DEFAULT_DETAILS_TABLE,
  private val supportsAddressChange: Boolean = true,
) {

  fun build() = InsuranceQuery.Data(
    contracts = contracts.map { c ->
      InsuranceQuery.Contract(
        __typename = Contract.type.name,
        typeOfContract = TypeOfContract.SE_HOUSE,
        id = "120e9ac9-84b1-4e5d-add1-70a9bad340be",
        status = InsuranceQuery.Status(
          __typename = c.typename,
          fragments = InsuranceQuery.Status.Fragments(
            contractStatusFragment = ContractStatusFragment(
              __typename = c.typename,
              asPendingStatus = if (c == ContractStatus.PENDING) {
                ContractStatusFragment.AsPendingStatus(
                  __typename = c.typename,
                  pendingSince = null,
                )
              } else {
                null
              },
              asActiveInFutureStatus = when (c) {
                ContractStatus.ACTIVE_IN_FUTURE -> ContractStatusFragment.AsActiveInFutureStatus(
                  __typename = c.typename,
                  futureInception = LocalDate.of(2025, 1, 1),
                )
                ContractStatus.ACTIVE_IN_FUTURE_INVALID ->
                  ContractStatusFragment.AsActiveInFutureStatus(
                    __typename = c.typename,
                    futureInception = null,
                  )
                else -> null
              },
              asActiveStatus = if (c == ContractStatus.ACTIVE) {
                ContractStatusFragment.AsActiveStatus(
                  __typename = c.typename,
                  pastInception = LocalDate.of(2021, 1, 6),
                  upcomingAgreementChange = if (showUpcomingAgreement) {
                    ContractStatusFragment.UpcomingAgreementChange(
                      newAgreement = ContractStatusFragment.NewAgreement(
                        __typename = SwedishApartmentAgreement.type.name,
                        asSwedishApartmentAgreement = ContractStatusFragment
                          .AsSwedishApartmentAgreement(
                            __typename = SwedishApartmentAgreement.type.name,
                            activeFrom = LocalDate.of(2021, 4, 6),
                          ),
                      ),
                    )
                  } else {
                    null
                  },
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
                  futureTermination = LocalDate.of(2034, 1, 1),
                )
              } else {
                null
              },
              asTerminatedInFutureStatus = null,
              asTerminatedTodayStatus = if (c == ContractStatus.TERMINATED_TODAY) {
                ContractStatusFragment.AsTerminatedTodayStatus(
                  __typename = c.typename,
                  today = LocalDate.now(),
                )
              } else {
                null
              },
              asTerminatedStatus = if (c == ContractStatus.TERMINATED) {
                ContractStatusFragment.AsTerminatedStatus(
                  __typename = c.typename,
                  termination = null,
                )
              } else {
                null
              },
            ),
          ),
        ),
        displayName = displayName,
        upcomingRenewal = renewal,
        currentAgreement = InsuranceQuery.CurrentAgreement(
          __typename = SwedishApartmentAgreement.type.name,
          asAgreementCore = InsuranceQuery.AsAgreementCore(
            __typename = SwedishApartmentAgreement.type.name,
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
                description = "Dina prylar är försäkrade till",
              ),
            ),
          ),
        ),
        termsAndConditions = InsuranceQuery.TermsAndConditions(
          displayName = "Terms and Conditions",
          url = "https://cdn.hedvig.com/info/insurance-terms-tenant-owners-2019-05.pdf",
        ),
        statusPills = emptyList(),
        detailPills = emptyList(),
        supportsAddressChange = supportsAddressChange,
        fragments = InsuranceQuery.Contract.Fragments(
          upcomingAgreementFragment = UpcomingAgreementFragment(
            status = UpcomingAgreementFragment.Status(
              __typename = c.typename,
              asActiveStatus = if (c == ContractStatus.ACTIVE) {
                UpcomingAgreementFragment.AsActiveStatus(
                  __typename = c.typename,
                  upcomingAgreementChange = if (showUpcomingAgreement) {
                    UpcomingAgreementFragment.UpcomingAgreementChange(
                      __typename = UpcomingAgreementChange.type.name,
                      fragments = UpcomingAgreementFragment.UpcomingAgreementChange.Fragments(
                        upcomingAgreementChangeFragment = UpcomingAgreementChangeFragment(
                          newAgreement = UpcomingAgreementChangeFragment.NewAgreement(
                            __typename = SwedishApartmentAgreement.type.name,
                            asAgreementCore = UpcomingAgreementChangeFragment
                              .AsAgreementCore(
                                __typename = SwedishApartmentAgreement.type.name,
                                activeFrom = LocalDate.of(2021, 4, 6),
                              ),
                          ),
                        ),
                      ),
                    )
                  } else {
                    null
                  },
                )
              } else {
                null
              },
              asTerminatedInFutureStatus = null,
              asTerminatedTodayStatus = null,
            ),
            upcomingAgreementDetailsTable = UpcomingAgreementFragment.UpcomingAgreementDetailsTable(
              __typename = Table.type.name,
              fragments = UpcomingAgreementFragment.UpcomingAgreementDetailsTable.Fragments(
                upcomingDetailsTable,
              ),
            ),
          ),
        ),
        logo = InsuranceQuery.Logo(
          variants = InsuranceQuery.Variants(
            __typename = IconVariants.type.name,
            fragments = InsuranceQuery.Variants.Fragments(
              IconVariantsFragment(
                dark = IconVariantsFragment.Dark(svgUrl = "https://www.example.com"),
                light = IconVariantsFragment.Light(svgUrl = "https://www.example.com"),
              ),
            ),
          ),
        ),
        upcomingAgreementDetailsTable = InsuranceQuery.UpcomingAgreementDetailsTable(
          __typename = "",
          fragments = InsuranceQuery.UpcomingAgreementDetailsTable.Fragments(
            TableFragment("", listOf()),
          ),
        ),
      )
    },
  )

  companion object {
    private val DEFAULT_DETAILS_TABLE = TableFragmentBuilder(
      title = "",
      sections = listOf(
        "Home details" to listOf(
          Triple("Adress", null, "Testvägen 1"),
          Triple("Postal code", null, "123 45"),
          Triple("Housing type", null, "Rental"),
          Triple("Size", null, "50 m2"),
        ),
        "Coinsured" to listOf(
          Triple("Insured people", null, "You + 1 person"),
        ),
      ),
    ).build()
  }
}
