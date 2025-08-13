import arrow.core.nonEmptyListOf
import arrow.core.right
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import assertk.assertions.isTrue
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.annotations.ApolloExperimental
import com.apollographql.apollo.testing.registerTestResponse
import com.hedvig.android.apollo.octopus.test.OctopusFakeResolver
import com.hedvig.android.apollo.test.TestApolloClientRule
import com.hedvig.android.apollo.test.TestNetworkTransportType
import com.hedvig.android.data.addons.data.GetTravelAddonBannerInfoUseCaseImpl
import com.hedvig.android.data.addons.data.TravelAddonBannerInfo
import com.hedvig.android.data.addons.data.TravelAddonBannerSource
import com.hedvig.android.data.addons.data.TravelAddonBannerSource.INSURANCES_TAB
import com.hedvig.android.data.addons.data.TravelAddonBannerSource.TRAVEL_CERTIFICATES
import com.hedvig.android.featureflags.flags.Feature
import com.hedvig.android.featureflags.flags.Feature.TRAVEL_ADDON
import com.hedvig.android.featureflags.test.FakeFeatureManager
import com.hedvig.android.logger.TestLogcatLoggingRule
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import octopus.TravelAddonBannerQuery
import octopus.type.UpsellTravelAddonFlow
import octopus.type.buildMember
import octopus.type.buildUpsellTravelAddonBanner
import org.junit.Rule
import org.junit.Test

class GetTravelAddonBannerInfoUseCaseImplTest {
  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()

  @get:Rule
  val testApolloClientRule = TestApolloClientRule(TestNetworkTransportType.MAP)

  @OptIn(ApolloExperimental::class)
  private val apolloClientWithError: ApolloClient
    get() = testApolloClientRule.apolloClient.apply {
      registerTestResponse(
        operation = TravelAddonBannerQuery(UpsellTravelAddonFlow.APP_UPSELL_UPGRADE),
        errors = listOf(com.apollographql.apollo.api.Error.Builder(message = "Bad message").build()),
      )
    }

  @OptIn(ApolloExperimental::class)
  private val apolloClientWithNullBannerData: ApolloClient
    get() = testApolloClientRule.apolloClient.apply {
      registerTestResponse(
        operation = TravelAddonBannerQuery(UpsellTravelAddonFlow.APP_UPSELL_UPGRADE),
        data = TravelAddonBannerQuery.Data(OctopusFakeResolver) {
          currentMember = buildMember {
            upsellTravelAddonBanner = null
          }
        },
      )
    }

  @OptIn(ApolloExperimental::class)
  private val apolloClientWithTwoFlows: ApolloClient
    get() = testApolloClientRule.apolloClient.apply {
      registerTestResponse(
        operation = TravelAddonBannerQuery(UpsellTravelAddonFlow.APP_UPSELL_UPGRADE),
        data = TravelAddonBannerQuery.Data(OctopusFakeResolver) {
          currentMember = buildMember {
            upsellTravelAddonBanner = buildUpsellTravelAddonBanner {
              badges = buildList {
                add("60 days")
              }
              contractIds = buildList {
                add("ContractId")
              }
              descriptionDisplayName = "Description"
              titleDisplayName = "Title"
            }
          }
        },
      )
      registerTestResponse(
        operation = TravelAddonBannerQuery(UpsellTravelAddonFlow.APP_ONLY_UPSALE),
        data = TravelAddonBannerQuery.Data(OctopusFakeResolver) {
          currentMember = buildMember {
            upsellTravelAddonBanner = null
          }
        },
      )
    }

  @OptIn(ApolloExperimental::class)
  private val apolloClientWithEmptyContracts: ApolloClient
    get() = testApolloClientRule.apolloClient.apply {
      registerTestResponse(
        operation = TravelAddonBannerQuery(UpsellTravelAddonFlow.APP_UPSELL_UPGRADE),
        data = TravelAddonBannerQuery.Data(OctopusFakeResolver) {
          currentMember = buildMember {
            upsellTravelAddonBanner = buildUpsellTravelAddonBanner {
              badges = buildList {
                add("60 days")
              }
              contractIds = listOf()
              descriptionDisplayName = "Description"
              titleDisplayName = "Title"
            }
          }
        },
      )
    }

  @OptIn(ApolloExperimental::class)
  private val apolloClientWithFullBannerData: ApolloClient
    get() = testApolloClientRule.apolloClient.apply {
      registerTestResponse(
        operation = TravelAddonBannerQuery(UpsellTravelAddonFlow.APP_UPSELL_UPGRADE),
        data = TravelAddonBannerQuery.Data(OctopusFakeResolver) {
          currentMember = buildMember {
            upsellTravelAddonBanner = buildUpsellTravelAddonBanner {
              badges = buildList {
                add("60 days")
              }
              contractIds = buildList {
                add("ContractId")
              }
              descriptionDisplayName = "Description"
              titleDisplayName = "Title"
            }
          }
        },
      )
    }

  @Test
  fun `if FF for addons is off return null`() = runTest {
    val featureManager = FakeFeatureManager(fixedMap = mapOf(TRAVEL_ADDON to false))
    val sut = GetTravelAddonBannerInfoUseCaseImpl(apolloClientWithTwoFlows, featureManager)
    val resultFromInsurances = sut.invoke(INSURANCES_TAB).first()
    assertThat(resultFromInsurances)
      .isEqualTo(null.right())
    val resultFromTravel = sut.invoke(TRAVEL_CERTIFICATES).first()
    assertThat(resultFromTravel)
      .isEqualTo(null.right())
  }

  @Test
  fun `if get null bannerData from BE return null`() = runTest {
    val featureManager = FakeFeatureManager(fixedMap = mapOf(Feature.TRAVEL_ADDON to true))
    val sut = GetTravelAddonBannerInfoUseCaseImpl(apolloClientWithNullBannerData, featureManager)
    val result = sut.invoke(TravelAddonBannerSource.TRAVEL_CERTIFICATES).first()
    assertk.assertThat(result)
      .isEqualTo(null.right())
  }

  @Test
  fun `the source is mapped to the correct flow for the query`() = runTest {
    val featureManager = FakeFeatureManager(fixedMap = mapOf(Feature.TRAVEL_ADDON to true))
    val sut = GetTravelAddonBannerInfoUseCaseImpl(apolloClientWithTwoFlows, featureManager)
    val resultFromTravelCertificates = sut.invoke(TravelAddonBannerSource.TRAVEL_CERTIFICATES).first().getOrNull()
    assertk.assertThat(resultFromTravelCertificates).isNotNull()
    val resultFromInsurances = sut.invoke(TravelAddonBannerSource.INSURANCES_TAB).first().getOrNull()
    assertk.assertThat(resultFromInsurances).isNull()
  }

  @Test
  fun `if get bannerData from BE is not null but contractIds are empty return null`() = runTest {
    val featureManager = FakeFeatureManager(fixedMap = mapOf(Feature.TRAVEL_ADDON to true))
    val sut = GetTravelAddonBannerInfoUseCaseImpl(apolloClientWithEmptyContracts, featureManager)
    val result = sut.invoke(TravelAddonBannerSource.TRAVEL_CERTIFICATES).first()
    assertk.assertThat(result)
      .isEqualTo(null.right())
  }

  @Test
  fun `if get error from BE return ErrorMessage`() = runTest {
    val featureManager = FakeFeatureManager(fixedMap = mapOf(Feature.TRAVEL_ADDON to true))
    val sut = GetTravelAddonBannerInfoUseCaseImpl(apolloClientWithError, featureManager)
    val resultFromTravels = sut.invoke(TravelAddonBannerSource.TRAVEL_CERTIFICATES).first().isLeft()
    assertk.assertThat(resultFromTravels)
      .isTrue()
  }

  @Test
  fun `if get full banner data from BE return TravelAddonBannerInfo`() = runTest {
    val featureManager = FakeFeatureManager(fixedMap = mapOf(Feature.TRAVEL_ADDON to true))
    val sut = GetTravelAddonBannerInfoUseCaseImpl(apolloClientWithFullBannerData, featureManager)
    val resultFromTravel = sut.invoke(TravelAddonBannerSource.TRAVEL_CERTIFICATES).first().getOrNull()
    assertk.assertThat(resultFromTravel)
      .isNotNull()
  }

  @Test
  fun `the received data is passed correctly and in full`() = runTest {
    val featureManager = FakeFeatureManager(fixedMap = mapOf(Feature.TRAVEL_ADDON to true))
    val sut = GetTravelAddonBannerInfoUseCaseImpl(apolloClientWithFullBannerData, featureManager)
    val resultFromTravel = sut.invoke(TravelAddonBannerSource.TRAVEL_CERTIFICATES).first().getOrNull()
    assertk.assertThat(resultFromTravel)
      .isEqualTo(
        TravelAddonBannerInfo(
          title = "Title",
          description = "Description",
          labels = listOf("60 days"),
          eligibleInsurancesIds = nonEmptyListOf("ContractId"),
        ),
      )
  }
}
