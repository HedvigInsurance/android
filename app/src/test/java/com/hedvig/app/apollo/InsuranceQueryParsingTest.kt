@file:OptIn(ApolloExperimental::class)

package com.hedvig.app.apollo

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import com.apollographql.apollo3.annotations.ApolloExperimental
import com.apollographql.apollo3.mockserver.enqueue
import com.hedvig.android.owldroid.graphql.InsuranceQuery
import com.hedvig.android.owldroid.graphql.test.InsuranceQuery_TestBuilder.Data
import com.hedvig.android.owldroid.graphql.type.AgreementStatus
import com.hedvig.android.owldroid.graphql.type.SwedishApartmentAgreement
import com.hedvig.android.typeadapter.PromiscuousLocalDateAdapter
import com.hedvig.app.testdata.dashboard.INSURANCE_DATA
import com.hedvig.app.testdata.dashboard.INSURANCE_DATA_TERMINATED
import com.hedvig.app.testdata.feature.insurance.INSURANCE_DATA_SWEDISH_HOUSE
import org.junit.Test
import java.time.LocalDate

class InsuranceQueryParsingTest {

  @Suppress("PrivatePropertyName")
  private val INSURANCE_DATA_from_test_builder by lazy {
    InsuranceQuery.Data(TestDataTestResolver) {
      activeContractBundles = emptyList()
      contracts = listOf(
        contract {
          id = "120e9ac9-84b1-4e5d-add1-70a9bad340be"
          logo = logo {
            variants = variants {
              dark = dark {
                svgUrl = "https://www.example.com"
              }
              light = light {
                svgUrl = "https://www.example.com"
              }
            }
          }
          status = activeStatusStatus {
            pastInception = PromiscuousLocalDateAdapter.toJsonStringForTestBuilder(
              LocalDate.of(2021, 1, 6),
            )
            upcomingAgreementChange = null
          }
          displayName = "Hemförsäkring"
          upcomingRenewal = upcomingRenewal {
            renewalDate = PromiscuousLocalDateAdapter.toJsonStringForTestBuilder(
              LocalDate.of(2021, 5, 6),
            )
            draftCertificateUrl = "https://www.example.com"
          }
          currentAgreement = agreementCoreCurrentAgreement {
            __typename = SwedishApartmentAgreement.type.name
            status = AgreementStatus.ACTIVE.rawValue
            certificateUrl = "https://www.example.com"
          }
          currentAgreementDetailsTable = currentAgreementDetailsTable {
            title = ""
            sections = listOf(
              section {
                title = "Home details"
                rows = listOf(
                  row {
                    title = "Adress"
                    subtitle = null
                    value = "Testvägen 1"
                  },
                  row {
                    title = "Postal code"
                    subtitle = null
                    value = "123 45"
                  },
                  row {
                    title = "Housing type"
                    subtitle = null
                    value = "Rental"
                  },
                  row {
                    title = "Size"
                    subtitle = null
                    value = "50 m2"
                  },
                )
              },
              section {
                title = "Coinsured"
                rows = listOf(
                  row {
                    title = "Insured people"
                    subtitle = null
                    value = "You + 1 person"
                  },
                )
              },
            )
          }
          contractPerils = List(6) {
            contractPeril {
              title = "Mock"
              description = "Mock"
              icon = icon {
                variants = variants {
                  dark = dark {
                    svgUrl = "/app-content-service/fire_dark.svg"
                  }
                  light = light {
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
            insurableLimit {
              label = "Utstyrene dine er forsikrat till"
              limit = "1 000 000 kr"
              description = "Dina prylar är försäkrade till"
            },
          )
          termsAndConditions = termsAndConditions {
            displayName = "Terms and Conditions"
            url = "https://cdn.hedvig.com/info/insurance-terms-tenant-owners-2019-05.pdf"
          }
          gradientOption = "GRADIENT_ONE"
          supportsAddressChange = true
          upcomingAgreementDetailsTable = upcomingAgreementDetailsTable {
            this.title = "Title"
            this.sections = emptyList()
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
        .query(InsuranceQuery(locale = com.hedvig.android.owldroid.graphql.type.Locale.sv_SE))
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
        .query(InsuranceQuery(locale = com.hedvig.android.owldroid.graphql.type.Locale.sv_SE))
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
      .query(InsuranceQuery(locale = com.hedvig.android.owldroid.graphql.type.Locale.sv_SE))
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
      .query(InsuranceQuery(locale = com.hedvig.android.owldroid.graphql.type.Locale.sv_SE))
      .execute()

    assertThat(response.data).isNotNull()
    assertThat(response.data!!).isEqualTo(originalData)
  }
}
