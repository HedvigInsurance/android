package com.hedvig.app.feature.home.model

import assertk.assertThat
import com.hedvig.android.hanalytics.featureflags.FeatureManager
import com.hedvig.android.hanalytics.featureflags.flags.Feature
import com.hedvig.android.hanalytics.test.FakeFeatureManager
import com.hedvig.app.testdata.feature.home.HOME_DATA_ACTIVE
import com.hedvig.app.testdata.feature.home.HOME_DATA_PAYIN_NEEDS_SETUP
import com.hedvig.app.util.containsNoneOfType
import com.hedvig.app.util.containsOfType
import com.hedvig.hanalytics.PaymentType
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.random.Random.Default.nextBoolean

class HomeItemsBuilderTest {

  @Test
  fun `when connect payin card-feature is disabled and payin is not connected, should not show connect payin`() =
    runTest {
      val featureManager: FeatureManager = FakeFeatureManager(
        featureMap = {
          mapOf(
            Feature.CONNECT_PAYIN_REMINDER to false,
            Feature.COMMON_CLAIMS to nextBoolean(),
          )
        },
      )
      val builder = HomeItemsBuilder(featureManager)

      val result = builder.buildItems(HOME_DATA_PAYIN_NEEDS_SETUP)

      assertThat(result).containsNoneOfType<HomeModel.ConnectPayin>()
    }

  @Test
  fun `when connect payin card-feature is enabled and payin is not connected, should show connect payin`() =
    runTest {
      val featureManager: FeatureManager = FakeFeatureManager(
        featureMap = {
          mapOf(
            Feature.CONNECT_PAYIN_REMINDER to true,
            Feature.COMMON_CLAIMS to nextBoolean(),
          )
        },
        paymentType = { enumValues<PaymentType>().random() },
      )
      val builder = HomeItemsBuilder(featureManager)

      val result = builder.buildItems(HOME_DATA_PAYIN_NEEDS_SETUP)

      assertThat(result).containsOfType<HomeModel.ConnectPayin>()
    }

  @Test
  fun `when common claims-feature is disabled, should not show common claims`() = runTest {
    val featureManager: FeatureManager = FakeFeatureManager(
      featureMap = { mapOf(Feature.COMMON_CLAIMS to false) },
    )
    val builder = HomeItemsBuilder(featureManager)

    val result = builder.buildItems(HOME_DATA_ACTIVE)

    assertThat(result).containsNoneOfType<HomeModel.CommonClaim>()
  }

  @Test
  fun `when common claims-feature is enabled, should show common claims`() = runTest {
    val featureManager: FeatureManager = FakeFeatureManager(
      featureMap = { mapOf(Feature.COMMON_CLAIMS to true) },
    )
    val builder = HomeItemsBuilder(featureManager)

    val result = builder.buildItems(HOME_DATA_ACTIVE)

    assertThat(result).containsOfType<HomeModel.CommonClaim>()
  }
}
