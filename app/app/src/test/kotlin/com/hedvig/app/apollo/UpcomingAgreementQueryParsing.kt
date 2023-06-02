package com.hedvig.app.apollo

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import com.apollographql.apollo3.annotations.ApolloExperimental
import com.apollographql.apollo3.mockserver.enqueue
import com.hedvig.android.apollo.giraffe.test.GiraffeFakeResolver
import com.hedvig.app.testdata.feature.changeaddress.UPCOMING_AGREEMENT_NONE
import giraffe.UpcomingAgreementQuery
import giraffe.fragment.TableFragment
import giraffe.fragment.UpcomingAgreementChangeFragment
import giraffe.fragment.UpcomingAgreementFragment
import giraffe.type.ActiveStatus
import giraffe.type.Locale
import giraffe.type.SwedishHouseAgreement
import giraffe.type.buildActiveStatus
import giraffe.type.buildContract
import giraffe.type.buildSwedishHouseAgreement
import giraffe.type.buildTable
import giraffe.type.buildTableRow
import giraffe.type.buildTableSection
import giraffe.type.buildUpcomingAgreementChange
import org.junit.Test
import java.time.LocalDate

@OptIn(ApolloExperimental::class)
class UpcomingAgreementQueryParsing {

  @Suppress("PrivatePropertyName")
  private val UPCOMING_AGREEMENT_SWEDISH_APARTMENT_with_custom_builder by lazy {
    UpcomingAgreementQuery.Data(
      contracts = listOf(
        UpcomingAgreementQuery.Contract(
          __typename = "",
          fragments = UpcomingAgreementQuery.Contract.Fragments(
            upcomingAgreementFragment = UpcomingAgreementFragment(
              upcomingAgreementDetailsTable = UpcomingAgreementFragment.UpcomingAgreementDetailsTable(
                __typename = "",
                fragments = UpcomingAgreementFragment.UpcomingAgreementDetailsTable.Fragments(
                  TableFragment(
                    title = "Detail",
                    sections = listOf(
                      TableFragment.Section(
                        title = "Address",
                        rows = listOf(
                          TableFragment.Row(
                            title = "Address",
                            subtitle = "Subtitle",
                            value = "Testgatan 123",
                          ),
                        ),
                      ),
                    ),
                  ),
                ),
              ),
              status = UpcomingAgreementFragment.Status(
                __typename = ActiveStatus.type.name, // This *must* be set
                asActiveStatus = UpcomingAgreementFragment.AsActiveStatus(
                  __typename = ActiveStatus.type.name, // This *must* be set
                  upcomingAgreementChange = UpcomingAgreementFragment.UpcomingAgreementChange(
                    __typename = "",
                    fragments = UpcomingAgreementFragment.UpcomingAgreementChange.Fragments(
                      upcomingAgreementChangeFragment = UpcomingAgreementChangeFragment(
                        newAgreement = UpcomingAgreementChangeFragment.NewAgreement(
                          __typename = SwedishHouseAgreement.type.name, // This *must* be set
                          asAgreementCore = UpcomingAgreementChangeFragment.AsAgreementCore(
                            __typename = SwedishHouseAgreement.type.name, // ktlint-disable max-line-length // This *must* be set
                            activeFrom = LocalDate.of(2021, 4, 11),
                          ),
                        ),
                      ),
                    ),
                  ),
                ),
                asTerminatedInFutureStatus = null,
                asTerminatedTodayStatus = null,
              ),
            ),
          ),
        ),
      ),
    )
  }

  @Suppress("PrivatePropertyName")
  private val UPCOMING_AGREEMENT_SWEDISH_APARTMENT_from_test_builder by lazy {
    UpcomingAgreementQuery.Data(GiraffeFakeResolver) {
      contracts = listOf(
        buildContract {
          upcomingAgreementDetailsTable = buildTable {
            title = "Detail"
            sections = listOf(
              buildTableSection {
                title = "Detail"
                rows = listOf(
                  buildTableRow {
                    title = "Address"
                    subtitle = "Subtitle"
                    value = "Testgatan 123"
                  },
                )
              },
            )
          }
          status = buildActiveStatus {
            upcomingAgreementChange = buildUpcomingAgreementChange {
              newAgreement = buildSwedishHouseAgreement {
                activeFrom = LocalDate.of(2021, 4, 11)
              }
            }
          }
        },
      )
    }
  }

  @Test
  fun `apollo parses an upcoming agreement constructed using apollo test builders`() =
    runApolloTest { mockServer, apolloClient ->
      val originalData = UPCOMING_AGREEMENT_SWEDISH_APARTMENT_from_test_builder
      val jsonData = originalData.toJsonStringWithData()
      mockServer.enqueue(jsonData)

      val response = apolloClient
        .query(UpcomingAgreementQuery(Locale.en_SE))
        .execute()

      assertThat(response.data).isNotNull()
      assertThat(response.data!!).isEqualTo(originalData)
      assertThat(
        response.data!!.contracts.first().fragments.upcomingAgreementFragment.status.asActiveStatus!!
          .upcomingAgreementChange!!.fragments.upcomingAgreementChangeFragment.newAgreement.asAgreementCore!!
          .activeFrom,
      ).isEqualTo(LocalDate.of(2021, 4, 11))
    }

  @Test
  fun `apollo parses an upcoming agreement constructed with our custom builders`() =
    runApolloTest { mockServer, apolloClient ->
      val originalData = UPCOMING_AGREEMENT_SWEDISH_APARTMENT_with_custom_builder
      val jsonData = originalData.toJsonStringWithData()
      mockServer.enqueue(jsonData)

      val response = apolloClient
        .query(UpcomingAgreementQuery(Locale.en_SE))
        .execute()

      assertThat(response.data).isNotNull()
      assertThat(response.data!!).isEqualTo(originalData)
      assertThat(
        response.data!!.contracts.first().fragments.upcomingAgreementFragment.status.asActiveStatus!!
          .upcomingAgreementChange!!.fragments.upcomingAgreementChangeFragment.newAgreement.asAgreementCore!!
          .activeFrom,
      ).isEqualTo(LocalDate.of(2021, 4, 11))
    }

  @Test
  fun `apollo parses an upcoming agreement response with no upcoming agreements`() =
    runApolloTest { mockServer, apolloClient ->
      val originalData = UPCOMING_AGREEMENT_NONE
      val jsonData = originalData.toJsonStringWithData()
      mockServer.enqueue(jsonData)

      val response = apolloClient
        .query(UpcomingAgreementQuery(Locale.en_SE))
        .execute()

      assertThat(response.data).isNotNull()
      assertThat(response.data!!).isEqualTo(originalData)
    }
}
