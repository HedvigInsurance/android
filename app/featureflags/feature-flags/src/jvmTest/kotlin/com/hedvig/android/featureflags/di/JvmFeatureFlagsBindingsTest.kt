package com.hedvig.android.featureflags.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.hedvig.android.core.buildconstants.AppBuildConfig
import com.hedvig.android.core.buildconstants.Flavor
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.featureflags.FeatureManager
import com.hedvig.android.featureflags.flags.JvmUnleashFeatureFlagProvider
import com.hedvig.android.logger.TestLogcatLoggingRule
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.createGraphFactory
import java.io.File
import kotlin.io.path.createTempDirectory
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertIs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import okio.Path.Companion.toOkioPath
import org.junit.Rule

@DependencyGraph(AppScope::class)
internal interface FeatureFlagsTestGraph {
  val featureManager: FeatureManager

  @DependencyGraph.Factory
  interface Factory {
    fun create(
      @Provides appBuildConfig: AppBuildConfig,
      @Provides dataStore: DataStore<Preferences>,
    ): FeatureFlagsTestGraph
  }
}

@OptIn(ExperimentalCoroutinesApi::class)
class JvmFeatureFlagsBindingsTest {
  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()

  @BeforeTest
  fun setUp() {
    Dispatchers.setMain(UnconfinedTestDispatcher())
  }

  @AfterTest
  fun tearDown() {
    Dispatchers.resetMain()
  }

  @Test
  fun `the feature manager resolves to the Unleash-backed JVM implementation`() {
    val dataStoreFile = File(createTempDirectory("feature-flags-test").toFile(), "test.preferences_pb")
    val dataStore = PreferenceDataStoreFactory.createWithPath { dataStoreFile.toOkioPath() }

    val graph = createGraphFactory<FeatureFlagsTestGraph.Factory>().create(TestAppBuildConfig, dataStore)

    assertIs<JvmUnleashFeatureFlagProvider>(graph.featureManager)
  }
}

private object TestAppBuildConfig : AppBuildConfig {
  override val debug: Boolean = true
  override val applicationId: String = "com.hedvig.dev.app"
  override val buildType: String = "debug"
  override val versionCode: Int = 1
  override val versionName: String = "1.0.0"
  override val appFlavor: Flavor = Flavor.Develop
  override val osReleaseVersion: String = "1"
  override val osSdkVersion: Int = 1
  override val brand: String = "brand"
  override val model: String = "model"
  override val device: String = "device"
  override val manufacturer: String = "manufacturer"
}
