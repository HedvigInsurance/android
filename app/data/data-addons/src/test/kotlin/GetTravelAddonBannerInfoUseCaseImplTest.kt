import arrow.core.raise.either
import assertk.assertions.isEqualTo
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.annotations.ApolloExperimental
import com.hedvig.android.apollo.test.TestApolloClientRule
import com.hedvig.android.apollo.test.TestNetworkTransportType
import com.hedvig.android.data.addons.data.GetTravelAddonBannerInfoUseCaseImpl
import com.hedvig.android.data.addons.data.TravelAddonBannerSource
import com.hedvig.android.featureflags.flags.Feature
import com.hedvig.android.featureflags.test.FakeFeatureManager2
import com.hedvig.android.logger.TestLogcatLoggingRule
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class GetTravelAddonBannerInfoUseCaseImplTest {
  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()

  @get:Rule
  val testApolloClientRule = TestApolloClientRule(TestNetworkTransportType.MAP)

  @OptIn(ApolloExperimental::class)
  private val apolloClient: ApolloClient
    get() = testApolloClientRule.apolloClient

  @Test
  fun `if FF for addons is off return null`() = runTest {
    val featureManager = FakeFeatureManager2(fixedMap = mapOf(Feature.TRAVEL_ADDON to false))
    val sut = GetTravelAddonBannerInfoUseCaseImpl(apolloClient, featureManager)
    val resultFromInsurances = sut.invoke(TravelAddonBannerSource.INSURANCES_TAB)
    assertk.assertThat(resultFromInsurances)
      .isEqualTo(either { null })
    val resultFromTravel = sut.invoke(TravelAddonBannerSource.TRAVEL_CERTIFICATES)
    assertk.assertThat(resultFromTravel)
      .isEqualTo(either { null })
  }
}
