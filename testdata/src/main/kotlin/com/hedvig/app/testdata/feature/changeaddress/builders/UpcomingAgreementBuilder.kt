package com.hedvig.app.testdata.feature.changeaddress.builders

import com.hedvig.android.apollo.graphql.fragment.TableFragment
import com.hedvig.android.apollo.graphql.fragment.UpcomingAgreementChangeFragment
import com.hedvig.android.apollo.graphql.fragment.UpcomingAgreementFragment
import com.hedvig.android.apollo.graphql.type.ActiveStatus
import com.hedvig.android.apollo.graphql.type.SwedishHouseAgreement
import com.hedvig.app.testdata.common.builders.TableFragmentBuilder
import java.time.LocalDate

class UpcomingAgreementBuilder(
  private val activeFrom: LocalDate = LocalDate.of(2021, 4, 11),
  private val newAgreement: UpcomingAgreementChangeFragment.NewAgreement =
    UpcomingAgreementChangeFragment.NewAgreement(
      __typename = SwedishHouseAgreement.type.name,
      asAgreementCore = UpcomingAgreementChangeFragment.AsAgreementCore(
        __typename = SwedishHouseAgreement.type.name,
        activeFrom = activeFrom,
      ),
    ),
  private val table: TableFragment = TableFragmentBuilder(
    title = "Details",
    sections = listOf(
      "Details" to listOf(
        Triple("Address", "Subtitle", "Testgatan 123"),
      ),
    ),
  ).build(),
) {

  fun build() = UpcomingAgreementFragment(
    upcomingAgreementDetailsTable = UpcomingAgreementFragment.UpcomingAgreementDetailsTable(
      __typename = "",
      fragments = UpcomingAgreementFragment.UpcomingAgreementDetailsTable.Fragments(
        table,
      ),
    ),
    status = UpcomingAgreementFragment.Status(
      __typename = ActiveStatus.type.name,
      asActiveStatus = UpcomingAgreementFragment.AsActiveStatus(
        __typename = ActiveStatus.type.name,
        upcomingAgreementChange = UpcomingAgreementFragment.UpcomingAgreementChange(
          __typename = "",
          fragments = UpcomingAgreementFragment.UpcomingAgreementChange.Fragments(
            upcomingAgreementChangeFragment = UpcomingAgreementChangeFragment(
              newAgreement = newAgreement,
            ),
          ),
        ),
      ),
      asTerminatedInFutureStatus = null,
      asTerminatedTodayStatus = null,
    ),
  )
}
