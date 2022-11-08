@file:OptIn(ApolloExperimental::class)

package com.hedvig.app.apollo

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import com.apollographql.apollo3.annotations.ApolloExperimental
import com.apollographql.apollo3.mockserver.enqueue
import com.hedvig.android.apollo.graphql.InsuranceQuery
import com.hedvig.android.apollo.graphql.type.AgreementStatus
import com.hedvig.android.apollo.graphql.type.TypeOfContractGradientOption
import com.hedvig.android.apollo.graphql.type.buildActiveStatus
import com.hedvig.android.apollo.graphql.type.buildContract
import com.hedvig.android.apollo.graphql.type.buildIcon
import com.hedvig.android.apollo.graphql.type.buildIconVariant
import com.hedvig.android.apollo.graphql.type.buildIconVariants
import com.hedvig.android.apollo.graphql.type.buildInsurableLimit
import com.hedvig.android.apollo.graphql.type.buildInsuranceTerm
import com.hedvig.android.apollo.graphql.type.buildPerilV2
import com.hedvig.android.apollo.graphql.type.buildSwedishApartmentAgreement
import com.hedvig.android.apollo.graphql.type.buildTable
import com.hedvig.android.apollo.graphql.type.buildTableRow
import com.hedvig.android.apollo.graphql.type.buildTableSection
import com.hedvig.android.apollo.graphql.type.buildUpcomingRenewal
import com.hedvig.app.testdata.dashboard.INSURANCE_DATA
import com.hedvig.app.testdata.dashboard.INSURANCE_DATA_TERMINATED
import com.hedvig.app.testdata.feature.insurance.INSURANCE_DATA_SWEDISH_HOUSE
import org.junit.Test
import java.time.LocalDate

class InsuranceQueryParsingTest {

  @Suppress("PrivatePropertyName")
  private val INSURANCE_DATA_from_test_builder by lazy {
    InsuranceQuery.Data(TestFakeResolver) {
      contracts = listOf(
        buildContract {
          id = "120e9ac9-84b1-4e5d-add1-70a9bad340be"
          logo = buildIcon {
            variants = buildIconVariants {
              dark = buildIconVariant {
                svgUrl = "https://www.example.com"
              }
              light = buildIconVariant {
                svgUrl = "https://www.example.com"
              }
            }
          }
          status = buildActiveStatus {
            pastInception = LocalDate.of(2021, 1, 6)
            upcomingAgreementChange = null
          }
          displayName = "Hemförsäkring"
          upcomingRenewal = buildUpcomingRenewal {
            renewalDate = LocalDate.of(2021, 5, 6)
            draftCertificateUrl = "https://www.example.com"
          }
          currentAgreement = buildSwedishApartmentAgreement {
            status = AgreementStatus.ACTIVE
            certificateUrl = "https://www.example.com"
          }
          currentAgreementDetailsTable = buildTable {
            title = ""
            sections = listOf(
              buildTableSection {
                title = "Home details"
                rows = listOf(
                  buildTableRow {
                    title = "Adress"
                    subtitle = null
                    value = "Testvägen 1"
                  },
                  buildTableRow {
                    title = "Postal code"
                    subtitle = null
                    value = "123 45"
                  },
                  buildTableRow {
                    title = "Housing type"
                    subtitle = null
                    value = "Rental"
                  },
                  buildTableRow {
                    title = "Size"
                    subtitle = null
                    value = "50 m2"
                  },
                )
              },
              buildTableSection {
                title = "Coinsured"
                rows = listOf(
                  buildTableRow {
                    title = "Insured people"
                    subtitle = null
                    value = "You + 1 person"
                  },
                )
              },
            )
          }
          contractPerils = List(6) {
            buildPerilV2 {
              title = "Mock"
              description = "Mock"
              icon = buildIcon {
                variants = buildIconVariants {
                  dark = buildIconVariant {
                    svgUrl = "/app-content-service/fire_dark.svg"
                  }
                  light = buildIconVariant {
                    svgUrl = "/app-content-service/fire.svg"
                  }
                }
              }
              covered = List(6) {
                "Covered"
              }
              exceptions = List(5) {
                "Exceptions"
              }
              info =
                "Du kan få ersättning om tvättmaskinen säckar ihop eller om annan elektrisk maskin eller apparat går sönder p.g.a. kortslutning, överslag eller överspänning." // ktlint-disable max-line-length
            }
          }
          insurableLimits = listOf(
            buildInsurableLimit {
              label = "Utstyrene dine er forsikrat till"
              limit = "1 000 000 kr"
              description = "Dina prylar är försäkrade till"
            },
          )
          termsAndConditions = buildInsuranceTerm {
            displayName = "Terms and Conditions"
            url = "https://cdn.hedvig.com/info/insurance-terms-tenant-owners-2019-05.pdf"
          }
          gradientOption = TypeOfContractGradientOption.GRADIENT_ONE
          supportsAddressChange = true
          upcomingAgreementDetailsTable = buildTable {
            title = "Title"
            sections = emptyList()
          }
        },
      )
    }
  }

  @Test
  fun `model from testbuilder and our own custom builders result in the same model`() {
    val testBuilderData = INSURANCE_DATA_from_test_builder
    val ownBuilderData = INSURANCE_DATA

    println("testBuilderData: $testBuilderData")
    println("ownBuilderData : $ownBuilderData")
    assertThat(testBuilderData).isEqualTo(ownBuilderData)
  }

  @Test
  fun `model from testbuilder and our own custom builders result in the same json`() {
    val testBuilderData = INSURANCE_DATA_from_test_builder
    val ownBuilderData = INSURANCE_DATA

    val testBuilderJson = testBuilderData.toJsonStringWithData()
    val ownBuilderJson = ownBuilderData.toJsonStringWithData()

    println("testBuilderJson: $testBuilderJson")
    println("ownBuilderJson : $ownBuilderJson")
    assertThat(testBuilderJson).isEqualTo(ownBuilderJson)
  }

  @Test
  fun `apollo parses an insurance constructed using apollo test builders`() =
    runApolloTest { mockServer, apolloClient ->
      val originalData = INSURANCE_DATA_from_test_builder
      val jsonData = originalData.toJsonStringWithData()
      mockServer.enqueue(jsonData)

      val response = apolloClient
        .query(InsuranceQuery(locale = com.hedvig.android.apollo.graphql.type.Locale.sv_SE))
        .execute()

      assertThat(response.data).isNotNull()
      assertThat(response.data!!).isEqualTo(originalData)
    }

  @Test
  fun `apollo parses an insurance constructed with our custom builders`() =
    runApolloTest { mockServer, apolloClient ->
      val originalData = INSURANCE_DATA
      val jsonData = originalData.toJsonStringWithData()
      mockServer.enqueue(jsonData)

      val response = apolloClient
        .query(InsuranceQuery(locale = com.hedvig.android.apollo.graphql.type.Locale.sv_SE))
        .execute()

      assertThat(response.data).isNotNull()
      assertThat(response.data!!).isEqualTo(originalData)
    }

  @Test
  fun `apollo parses an insurance for swedish house`() = runApolloTest { mockServer, apolloClient ->
    val originalData = INSURANCE_DATA_SWEDISH_HOUSE
    val jsonData = originalData.toJsonStringWithData()
    mockServer.enqueue(jsonData)

    val response = apolloClient
      .query(InsuranceQuery(locale = com.hedvig.android.apollo.graphql.type.Locale.sv_SE))
      .execute()

    assertThat(response.data).isNotNull()
    assertThat(response.data!!).isEqualTo(originalData)
  }

  @Test
  fun `apollo parses an insurance which is terminated`() = runApolloTest { mockServer, apolloClient ->
    val originalData = INSURANCE_DATA_TERMINATED
    val jsonData = originalData.toJsonStringWithData()
    mockServer.enqueue(jsonData)

    val response = apolloClient
      .query(InsuranceQuery(locale = com.hedvig.android.apollo.graphql.type.Locale.sv_SE))
      .execute()

    assertThat(response.data).isNotNull()
    assertThat(response.data!!).isEqualTo(originalData)
  }
}
