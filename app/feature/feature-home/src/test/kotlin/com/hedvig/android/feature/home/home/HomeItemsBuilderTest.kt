package com.hedvig.android.feature.home.home

import assertk.assertThat
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isNotEmpty
import assertk.assertions.size
import com.hedvig.android.apollo.giraffe.test.GiraffeFakeResolver
import com.hedvig.android.hanalytics.featureflags.FeatureManager
import com.hedvig.android.hanalytics.featureflags.flags.Feature
import com.hedvig.android.hanalytics.featureflags.test.FakeFeatureManager
import com.hedvig.app.feature.home.model.HomeModel
import com.hedvig.hanalytics.PaymentType
import giraffe.HomeQuery
import giraffe.type.PayinMethodStatus
import giraffe.type.buildActiveStatus
import giraffe.type.buildCommonClaim
import giraffe.type.buildContract
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.random.Random

class HomeItemsBuilderTest {
  private val homeDataActiveWithCommonClaim = HomeQuery.Data(GiraffeFakeResolver) {
    contracts = listOf(
      buildContract {
        status = buildActiveStatus({})
      },
    )
    commonClaims = listOf(
      buildCommonClaim {},
    )
  }

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

  @Test
  fun `when common claims-feature is enabled, should show common claims`() = runTest {
    val featureManager: FeatureManager = FakeFeatureManager(
      featureMap = {
        mapOf(Feature.COMMON_CLAIMS to true)
      },
    )
    val builder = HomeItemsBuilder(featureManager)

    val result = builder.buildItems(
      homeData = homeDataActiveWithCommonClaim,
      showTravelCertificate = false,
    )

    assertThat(result.filterIsInstance<HomeModel.CommonClaims>()).isNotEmpty()
  }

  @Test
  fun `when common claims-feature is disabled, and there are no travel certificates should not show common claims`() =
    runTest {
      val featureManager: FeatureManager = FakeFeatureManager(
        featureMap = {
          mapOf(Feature.COMMON_CLAIMS to false)
        },
      )
      val builder = HomeItemsBuilder(featureManager)

      val result = builder.buildItems(
        homeData = homeDataActiveWithCommonClaim,
        showTravelCertificate = false,
      )

      assertThat(result.filterIsInstance<HomeModel.CommonClaims>()).isEmpty()
      assertThat(result.filterIsInstance<HomeModel.Header>()).size().isEqualTo(1)
    }

  @Test
  fun `when common claims-feature is disabled, but there are travel certificates, should show common claims`() =
    runTest {
      val featureManager: FeatureManager = FakeFeatureManager(
        featureMap = {
          mapOf(Feature.COMMON_CLAIMS to false)
        },
      )
      val builder = HomeItemsBuilder(featureManager)

      val result = builder.buildItems(
        homeData = homeDataActiveWithCommonClaim,
        showTravelCertificate = true,
      )

      assertThat(result.filterIsInstance<HomeModel.CommonClaims>()).isNotEmpty()
      assertThat(result.filterIsInstance<HomeModel.Header>()).size().isEqualTo(2)
    }

  // ktlint-disable max-line-length
  @Test
  fun `when common claims-feature is disabled, and there are no travel certificates, should not show common claims`() =
    runTest {
      val featureManager: FeatureManager = FakeFeatureManager(
        featureMap = {
          mapOf(Feature.COMMON_CLAIMS to false)
        },
      )
      val builder = HomeItemsBuilder(featureManager)

      val result = builder.buildItems(
        homeData = homeDataActiveWithCommonClaim,
        showTravelCertificate = false,
      )

      assertThat(result.filterIsInstance<HomeModel.CommonClaims>()).isEmpty()
      assertThat(result.filterIsInstance<HomeModel.Header>()).size().isEqualTo(1)
    }
}
