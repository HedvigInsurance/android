package com.hedvig.app.testdata.feature.insurance.builders

import com.hedvig.app.testdata.common.builders.TableFragmentBuilder
import giraffe.InsuranceQuery
import giraffe.fragment.ContractStatusFragment
import giraffe.fragment.IconVariantsFragment
import giraffe.fragment.InsurableLimitsFragment
import giraffe.fragment.TableFragment
import giraffe.fragment.UpcomingAgreementChangeFragment
import giraffe.fragment.UpcomingAgreementFragment
import giraffe.type.ActiveStatus
import giraffe.type.AgreementStatus
import giraffe.type.Contract
import giraffe.type.NorwegianTravelAgreement
import giraffe.type.SwedishApartmentAgreement
import giraffe.type.TypeOfContract
import giraffe.type.TypeOfContractGradientOption
import java.time.LocalDate

class InsuranceContractBuilder(
  private val renewal: InsuranceQuery.UpcomingRenewal? =
    InsuranceQuery.UpcomingRenewal(
      renewalDate = LocalDate.now(),
      draftCertificateUrl = "https://www.example.com",
    ),
  private val agreementStatus: AgreementStatus = AgreementStatus.ACTIVE,
  private val showUpcomingAgreement: Boolean = false,
  private val detailsTable: TableFragment = TableFragmentBuilder().build(),
  private val supportsAddressChange: Boolean = true,
) {

  fun build() = InsuranceQuery.Contract(
    __typename = Contract.type.name,
    typeOfContract = TypeOfContract.SE_HOUSE,
    id = "120e9ac9-84b1-4e5d-add1-70a9bad340be",
    status = InsuranceQuery.Status(
      __typename = ActiveStatus.type.name,
      fragments = InsuranceQuery.Status.Fragments(
        contractStatusFragment = ContractStatusFragment(
          __typename = ActiveStatus.type.name,
          asPendingStatus = null,
          asActiveInFutureStatus = null,
          asActiveStatus = ContractStatusFragment.AsActiveStatus(
            __typename = ActiveStatus.type.name,
            pastInception = LocalDate.of(2020, 2, 1),
            upcomingAgreementChange = if (showUpcomingAgreement) {
              ContractStatusFragment.UpcomingAgreementChange(
                newAgreement = ContractStatusFragment.NewAgreement(
                  __typename = SwedishApartmentAgreement.type.name,
                  asSwedishApartmentAgreement = ContractStatusFragment.AsSwedishApartmentAgreement(
                    __typename = SwedishApartmentAgreement.type.name,
                    activeFrom = LocalDate.of(2021, 4, 6),
                  ),
                ),
              )
            } else {
              null
            },
          ),
          asActiveInFutureAndTerminatedInFutureStatus = null,
          asTerminatedInFutureStatus = null,
          asTerminatedTodayStatus = null,
          asTerminatedStatus = null,
        ),
      ),
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
    gradientOption = TypeOfContractGradientOption.GRADIENT_ONE,
    supportsAddressChange = supportsAddressChange,
    fragments = InsuranceQuery.Contract.Fragments(
      upcomingAgreementFragment = UpcomingAgreementFragment(
        status = UpcomingAgreementFragment.Status(
          __typename = ActiveStatus.type.name,
          asActiveStatus = UpcomingAgreementFragment.AsActiveStatus(
            __typename = ActiveStatus.type.name,
            upcomingAgreementChange = if (showUpcomingAgreement) {
              UpcomingAgreementFragment.UpcomingAgreementChange(
                __typename = "",
                fragments = UpcomingAgreementFragment.UpcomingAgreementChange.Fragments(
                  upcomingAgreementChangeFragment = UpcomingAgreementChangeFragment(
                    newAgreement = UpcomingAgreementChangeFragment.NewAgreement(
                      __typename = SwedishApartmentAgreement.type.name,
                      asAgreementCore = UpcomingAgreementChangeFragment.AsAgreementCore(
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
          ),
          asTerminatedTodayStatus = null,
          asTerminatedInFutureStatus = null,
        ),
        upcomingAgreementDetailsTable = UpcomingAgreementFragment.UpcomingAgreementDetailsTable(
          __typename = "",
          fragments = UpcomingAgreementFragment.UpcomingAgreementDetailsTable.Fragments(
            TableFragmentBuilder().build(),
          ),
        ),
      ),
    ),
    logo = InsuranceQuery.Logo(
      variants = InsuranceQuery.Variants(
        __typename = "",
        fragments = InsuranceQuery.Variants.Fragments(
          IconVariantsFragment(
            dark = IconVariantsFragment.Dark(svgUrl = "https://www.example.com"),
            light = IconVariantsFragment.Light(svgUrl = "https://www.example.com"),
          ),
        ),
      ),
    ),
  )
}
