package com.hedvig.app.feature.home.model

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.size
import com.hedvig.android.feature.travelcertificate.data.TravelCertificateResult
import com.hedvig.android.hanalytics.featureflags.FeatureManager
import com.hedvig.android.hanalytics.featureflags.flags.Feature
import com.hedvig.android.hanalytics.featureflags.test.FakeFeatureManager
import com.hedvig.app.testdata.feature.home.HOME_DATA_ACTIVE
import com.hedvig.app.testdata.feature.home.HOME_DATA_PAYIN_NEEDS_SETUP
import com.hedvig.app.util.containsNoneOfType
import com.hedvig.app.util.containsOfType
import com.hedvig.hanalytics.PaymentType
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import org.junit.Test
import kotlin.random.Random

class HomeItemsBuilderTest {

  @Test
  fun `when connect payin card-feature is disabled and payin is not connected, should not show connect payin`() =
    runTest {
      val featureManager: FeatureManager = FakeFeatureManager(
        featureMap = {
          mapOf(
            Feature.CONNECT_PAYIN_REMINDER to false,
            Feature.COMMON_CLAIMS to Random.nextBoolean(),
            Feature.TRAVEL_CERTIFICATE to true,
          )
        },
      )
      val builder = HomeItemsBuilder(featureManager)

      val result = builder.buildItems(HOME_DATA_PAYIN_NEEDS_SETUP, null)

      assertThat(result).containsNoneOfType<HomeModel.ConnectPayin>()
    }

  @Test
  fun `when connect payin card-feature is enabled and payin is not connected, should show connect payin`() =
    runTest {
      val featureManager: FeatureManager = FakeFeatureManager(
        featureMap = {
          mapOf(
            Feature.CONNECT_PAYIN_REMINDER to true,
            Feature.COMMON_CLAIMS to Random.nextBoolean(),
            Feature.TRAVEL_CERTIFICATE to true,
          )
        },
        paymentType = { enumValues<PaymentType>().random() },
      )
      val builder = HomeItemsBuilder(featureManager)

      val result = builder.buildItems(HOME_DATA_PAYIN_NEEDS_SETUP, null)

      assertThat(result).containsOfType<HomeModel.ConnectPayin>()
    }

  @Test
  fun `when common claims-feature is enabled, should show common claims`() = runTest {
    val featureManager: FeatureManager = FakeFeatureManager(
      featureMap = {
        mapOf(
          Feature.COMMON_CLAIMS to true,
          Feature.TRAVEL_CERTIFICATE to true,
        )
      },
    )
    val builder = HomeItemsBuilder(featureManager)

    val result = builder.buildItems(HOME_DATA_ACTIVE, null)

    assertThat(result).containsOfType<HomeModel.CommonClaims>()
  }

  @Test
  fun `when common claims-feature is disabled, and there are no travel certificates should not show common claims`() =
    runTest {
      val featureManager: FeatureManager = FakeFeatureManager(
        featureMap = {
          mapOf(
            Feature.COMMON_CLAIMS to false,
            Feature.TRAVEL_CERTIFICATE to true,
          )
        },
      )
      val builder = HomeItemsBuilder(featureManager)

      val result = builder.buildItems(HOME_DATA_ACTIVE, null)

      assertThat(result).containsNoneOfType<HomeModel.CommonClaims>()
      assertThat(result.filterIsInstance<HomeModel.Header>()).size().isEqualTo(1)
    }

  @Test
  fun `when common claims-feature is disabled, but there are travel certificates, should show common claims`() =
    runTest {
      val featureManager: FeatureManager = FakeFeatureManager(
        featureMap = {
          mapOf(
            Feature.COMMON_CLAIMS to false,
            Feature.TRAVEL_CERTIFICATE to true,
          )
        },
      )
      val builder = HomeItemsBuilder(featureManager)

      val result = builder.buildItems(
        HOME_DATA_ACTIVE,
        TravelCertificateResult.TravelCertificateSpecifications(
          "contractId",
          "email",
          1,
          LocalDate.parse("1970-01-01")..LocalDate.parse("1970-01-01"),
          1,
        ),
      )

      assertThat(result).containsOfType<HomeModel.CommonClaims>()
      assertThat(result.filterIsInstance<HomeModel.Header>()).size().isEqualTo(2)
    }

  // ktlint-disable max-line-length
  @Test
  fun `when common claims-feature and travel certificate feature is disabled, and there are travel certificates, should not show common claims`() =
    runTest {
      val featureManager: FeatureManager = FakeFeatureManager(
        featureMap = {
          mapOf(
            Feature.COMMON_CLAIMS to false,
            Feature.TRAVEL_CERTIFICATE to false,
          )
        },
      )
      val builder = HomeItemsBuilder(featureManager)

      val result = builder.buildItems(
        HOME_DATA_ACTIVE,
        TravelCertificateResult.TravelCertificateSpecifications(
          "contractId",
          "email",
          1,
          LocalDate.parse("1970-01-01")..LocalDate.parse("1970-01-01"),
          1,
        ),
      )

      assertThat(result).containsNoneOfType<HomeModel.CommonClaims>()
      assertThat(result.filterIsInstance<HomeModel.Header>()).size().isEqualTo(1)
    }
}
