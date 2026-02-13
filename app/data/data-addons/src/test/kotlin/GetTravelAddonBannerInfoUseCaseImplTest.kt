import arrow.core.nonEmptyListOf
import arrow.core.right
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isTrue
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.annotations.ApolloExperimental
import com.apollographql.apollo.api.Error
import com.apollographql.apollo.testing.registerTestResponse
import com.hedvig.android.apollo.octopus.test.OctopusFakeResolver
import com.hedvig.android.apollo.test.TestApolloClientRule
import com.hedvig.android.apollo.test.TestNetworkTransportType
import com.hedvig.android.data.addons.data.AddonBannerInfo
import com.hedvig.android.data.addons.data.AddonBannerSource.INSURANCES_TAB
import com.hedvig.android.data.addons.data.AddonBannerSource.TRAVEL_CERTIFICATES
import com.hedvig.android.data.addons.data.FlowType
import com.hedvig.android.data.addons.data.GetAddonBannerInfoUseCaseImpl
import com.hedvig.android.featureflags.flags.Feature.TRAVEL_ADDON
import com.hedvig.android.featureflags.test.FakeFeatureManager
import com.hedvig.android.logger.TestLogcatLoggingRule
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import octopus.AddonBannersQuery
import octopus.type.AddonFlow
import octopus.type.buildAddonBanner
import octopus.type.buildMember
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
        operation = AddonBannersQuery(listOf(AddonFlow.APP_TRAVEL_PLUS_SELL_OR_UPGRADE)),
        errors = listOf(Error.Builder(message = "Bad message").build()),
      )
    }

  @OptIn(ApolloExperimental::class)
  private val apolloClientWithNullBannerData: ApolloClient
    get() = testApolloClientRule.apolloClient.apply {
      registerTestResponse(
        operation = AddonBannersQuery(listOf(AddonFlow.APP_TRAVEL_PLUS_SELL_OR_UPGRADE)),
        data = AddonBannersQuery.Data(OctopusFakeResolver) {
          currentMember = buildMember {
            addonBanners = emptyList()
          }
        },
      )
    }

  @OptIn(ApolloExperimental::class)
  private val apolloClientWithTwoFlows: ApolloClient
    get() = testApolloClientRule.apolloClient.apply {
      registerTestResponse(
        operation = AddonBannersQuery(listOf(AddonFlow.APP_TRAVEL_PLUS_SELL_OR_UPGRADE)),
        data = AddonBannersQuery.Data(OctopusFakeResolver) {
          currentMember = buildMember {
            addonBanners = listOf(
              buildAddonBanner {
                badges = buildList {
                  add("60 days")
                }
                contractIds = buildList {
                  add("ContractId")
                }
                descriptionDisplayName = "Description"
                displayTitleName = "Title"
                flow = AddonFlow.APP_TRAVEL_PLUS_SELL_OR_UPGRADE
              }
            )
          }
        },
      )
      registerTestResponse(
        operation = AddonBannersQuery(listOf(AddonFlow.APP_CAR_PLUS, AddonFlow.APP_TRAVEL_PLUS_SELL_ONLY)),
        data = AddonBannersQuery.Data(OctopusFakeResolver) {
          currentMember = buildMember {
            addonBanners = emptyList()
          }
        },
      )
    }

  @OptIn(ApolloExperimental::class)
  private val apolloClientWithEmptyContracts: ApolloClient
    get() = testApolloClientRule.apolloClient.apply {
      registerTestResponse(
        operation = AddonBannersQuery(listOf(AddonFlow.APP_TRAVEL_PLUS_SELL_OR_UPGRADE)),
        data = AddonBannersQuery.Data(OctopusFakeResolver) {
          currentMember = buildMember {
            addonBanners = listOf(
              buildAddonBanner {
                badges = buildList {
                  add("60 days")
                }
                contractIds = emptyList()
                descriptionDisplayName = "Description"
                displayTitleName = "Title"
                flow = AddonFlow.APP_TRAVEL_PLUS_SELL_OR_UPGRADE
              }
            )
          }
        },
      )
    }

  @OptIn(ApolloExperimental::class)
  private val apolloClientWithFullBannerData: ApolloClient
    get() = testApolloClientRule.apolloClient.apply {
      registerTestResponse(
        operation = AddonBannersQuery(listOf(AddonFlow.APP_TRAVEL_PLUS_SELL_OR_UPGRADE)),
        data = AddonBannersQuery.Data(OctopusFakeResolver) {
          currentMember = buildMember {
            addonBanners = listOf(
              buildAddonBanner {
                badges = buildList {
                  add("60 days")
                }
                contractIds = buildList {
                  add("ContractId")
                }
                descriptionDisplayName = "Description"
                displayTitleName = "Title"
                flow = AddonFlow.APP_TRAVEL_PLUS_SELL_OR_UPGRADE
              }
            )
          }
        },
      )
    }

  @Test
  fun `if FF for addons is off return empty list`() = runTest {
    val featureManager = FakeFeatureManager(fixedMap = mapOf(TRAVEL_ADDON to false))
    val sut = GetAddonBannerInfoUseCaseImpl(apolloClientWithTwoFlows, featureManager)
    val resultFromInsurances = sut.invoke(INSURANCES_TAB).first()
    assertThat(resultFromInsurances)
      .isEqualTo(emptyList<AddonBannerInfo>().right())
    val resultFromTravel = sut.invoke(TRAVEL_CERTIFICATES).first()
    assertThat(resultFromTravel)
      .isEqualTo(emptyList<AddonBannerInfo>().right())
  }

  @Test
  fun `if get null bannerData from BE return empty list`() = runTest {
    val featureManager = FakeFeatureManager(fixedMap = mapOf(TRAVEL_ADDON to true))
    val sut = GetAddonBannerInfoUseCaseImpl(apolloClientWithNullBannerData, featureManager)
    val result = sut.invoke(TRAVEL_CERTIFICATES).first()
    assertThat(result)
      .isEqualTo(emptyList<AddonBannerInfo>().right())
  }

  @Test
  fun `the source is mapped to the correct flow for the query`() = runTest {
    val featureManager = FakeFeatureManager(fixedMap = mapOf(TRAVEL_ADDON to true))
    val sut = GetAddonBannerInfoUseCaseImpl(apolloClientWithTwoFlows, featureManager)
    val resultFromTravelCertificates = sut.invoke(TRAVEL_CERTIFICATES).first().getOrNull()
    assertThat(resultFromTravelCertificates).isNotNull()
    val resultFromInsurances = sut.invoke(INSURANCES_TAB).first().getOrNull()
    assertThat(resultFromInsurances).isEqualTo(emptyList())
  }

  @Test
  fun `if get bannerData from BE is not null but contractIds are empty return empty list`() = runTest {
    val featureManager = FakeFeatureManager(fixedMap = mapOf(TRAVEL_ADDON to true))
    val sut = GetAddonBannerInfoUseCaseImpl(apolloClientWithEmptyContracts, featureManager)
    val result = sut.invoke(TRAVEL_CERTIFICATES).first()
    assertThat(result)
      .isEqualTo(emptyList<AddonBannerInfo>().right())
  }

  @Test
  fun `if get error from BE return ErrorMessage`() = runTest {
    val featureManager = FakeFeatureManager(fixedMap = mapOf(TRAVEL_ADDON to true))
    val sut = GetAddonBannerInfoUseCaseImpl(apolloClientWithError, featureManager)
    val resultFromTravels = sut.invoke(TRAVEL_CERTIFICATES).first().isLeft()
    assertThat(resultFromTravels)
      .isTrue()
  }

  @Test
  fun `if get full banner data from BE return TravelAddonBannerInfo`() = runTest {
    val featureManager = FakeFeatureManager(fixedMap = mapOf(TRAVEL_ADDON to true))
    val sut = GetAddonBannerInfoUseCaseImpl(apolloClientWithFullBannerData, featureManager)
    val resultFromTravel = sut.invoke(TRAVEL_CERTIFICATES).first().getOrNull()
    assertThat(resultFromTravel)
      .isNotNull()
  }

  @Test
  fun `the received data is passed correctly and in full`() = runTest {
    val featureManager = FakeFeatureManager(fixedMap = mapOf(TRAVEL_ADDON to true))
    val sut = GetAddonBannerInfoUseCaseImpl(apolloClientWithFullBannerData, featureManager)
    val resultFromTravel = sut.invoke(TRAVEL_CERTIFICATES).first().getOrNull()
    assertThat(resultFromTravel)
      .isEqualTo(
        listOf(
          AddonBannerInfo(
            title = "Title",
            description = "Description",
            labels = listOf("60 days"),
            eligibleInsurancesIds = nonEmptyListOf("ContractId"),
            flowType = FlowType.APP_TRAVEL_PLUS_SELL_OR_UPGRADE,
          ),
        ),
      )
  }
}
