package com.hedvig.app.testdata.feature.changeaddress

import com.hedvig.android.apollo.graphql.ActiveContractBundlesQuery
import com.hedvig.android.apollo.graphql.UpcomingAgreementQuery
import com.hedvig.android.apollo.graphql.fragment.UpcomingAgreementFragment
import com.hedvig.app.testdata.common.builders.TableFragmentBuilder
import com.hedvig.app.testdata.feature.changeaddress.builders.ActiveContractBundlesBuilder
import com.hedvig.app.testdata.feature.changeaddress.builders.UpcomingAgreementBuilder

val UPCOMING_AGREEMENT_NONE = UpcomingAgreementQuery.Data(
  contracts = listOf(
    UpcomingAgreementQuery.Contract(
      __typename = "",
      fragments = UpcomingAgreementQuery.Contract.Fragments(
        upcomingAgreementFragment = UpcomingAgreementFragment(
          upcomingAgreementDetailsTable = UpcomingAgreementFragment.UpcomingAgreementDetailsTable(
            __typename = "",
            fragments = UpcomingAgreementFragment.UpcomingAgreementDetailsTable.Fragments(
              TableFragmentBuilder().build(),
            ),
          ),
          status = UpcomingAgreementFragment.Status(
            __typename = "",
            asActiveStatus = null,
            asTerminatedInFutureStatus = null,
            asTerminatedTodayStatus = null,
          ),
        ),
      ),
    ),
  ),
)

val UPCOMING_AGREEMENT_SWEDISH_APARTMENT = UpcomingAgreementQuery.Data(
  contracts = listOf(
    UpcomingAgreementQuery.Contract(
      __typename = "",
      fragments = UpcomingAgreementQuery.Contract.Fragments(
        upcomingAgreementFragment = UpcomingAgreementBuilder().build(),
      ),
    ),
  ),
)

val SELF_CHANGE_ELIGIBILITY = ActiveContractBundlesQuery.Data(
  listOf(
    ActiveContractBundlesBuilder(
      embarkStoryId = "testId",
    ).build(),
  ),
)

val BLOCKED_SELF_CHANGE_ELIGIBILITY = ActiveContractBundlesQuery.Data(
  listOf(),
)
