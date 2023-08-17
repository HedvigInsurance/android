package com.hedvig.android.feature.home.home

import assertk.assertThat
import assertk.assertions.isEmpty
import assertk.assertions.isNotEmpty
import com.hedvig.android.apollo.giraffe.test.GiraffeFakeResolver
import com.hedvig.android.hanalytics.featureflags.FeatureManager
import com.hedvig.android.hanalytics.featureflags.flags.Feature
import com.hedvig.android.hanalytics.featureflags.test.FakeFeatureManager
import com.hedvig.app.feature.home.model.HomeModel
import com.hedvig.hanalytics.PaymentType
import giraffe.HomeQuery
import giraffe.type.PayinMethodStatus
import giraffe.type.buildActiveStatus
import giraffe.type.buildContract
import kotlin.random.Random
import kotlinx.coroutines.test.runTest
import org.junit.Test

class HomeItemsBuilderTest {

  private val homeDataPayinNeedsSetup = HomeQuery.Data(GiraffeFakeResolver) {
    contracts = listOf(
      buildContract {
        status = buildActiveStatus({})
      },
    )
    payinMethodStatus = PayinMethodStatus.NEEDS_SETUP
  }

  private val homeDataPayinAlreadySetup = HomeQuery.Data(GiraffeFakeResolver) {
    contracts = listOf(
      buildContract {
        status = buildActiveStatus({})
      },
    )
    payinMethodStatus = PayinMethodStatus.ACTIVE
  }

  @Test
  fun `when connect payin card-feature is disabled and payin is not connected, should not show connect payin`() {
    runTest {
      val featureManager: FeatureManager = FakeFeatureManager(
        featureMap = {
          mapOf(
            Feature.CONNECT_PAYIN_REMINDER to false,
            Feature.COMMON_CLAIMS to Random.nextBoolean(),
          )
        },
      )
      val builder = HomeItemsBuilder(featureManager)

      val result = builder.buildItems(
        homeDataPayinNeedsSetup,
        false,
      )

      assertThat(result.filterIsInstance<HomeModel.ConnectPayin>()).isEmpty()
    }
  }

  @Test
  fun `when connect payin card-feature is enabled and payin is not connected, should show connect payin`() =
    runTest {
      val featureManager: FeatureManager = FakeFeatureManager(
        featureMap = {
          mapOf(
            Feature.CONNECT_PAYIN_REMINDER to true,
            Feature.COMMON_CLAIMS to Random.nextBoolean(),
          )
        },
        paymentType = { enumValues<PaymentType>().random() },
      )
      val builder = HomeItemsBuilder(featureManager)

      val result = builder.buildItems(homeDataPayinNeedsSetup, false)

      assertThat(result.filterIsInstance<HomeModel.ConnectPayin>()).isNotEmpty()
    }

  @Test
  fun `when connect payin card-feature is enabled and payin is connected already, should not show connect payin`() =
    runTest {
      val featureManager: FeatureManager = FakeFeatureManager(
        featureMap = {
          mapOf(
            Feature.CONNECT_PAYIN_REMINDER to true,
            Feature.COMMON_CLAIMS to Random.nextBoolean(),
          )
        },
        paymentType = { enumValues<PaymentType>().random() },
      )
      val builder = HomeItemsBuilder(featureManager)

      val result = builder.buildItems(homeDataPayinAlreadySetup, false)

      assertThat(result.filterIsInstance<HomeModel.ConnectPayin>()).isEmpty()
    }
}
