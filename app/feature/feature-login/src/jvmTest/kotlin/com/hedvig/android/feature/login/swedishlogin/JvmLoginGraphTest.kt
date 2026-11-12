package com.hedvig.android.feature.login.swedishlogin

import androidx.lifecycle.SavedStateHandle
import assertk.assertThat
import assertk.assertions.containsOnly
import assertk.assertions.isFalse
import assertk.assertions.isInstanceOf
import com.hedvig.android.auth.AuthTokenService
import com.hedvig.android.auth.BankIdSandboxState
import com.hedvig.android.auth.test.FakeAuthRepository
import com.hedvig.android.auth.test.TestAuthTokenService
import com.hedvig.android.authlib.AuthRepository
import com.hedvig.android.core.buildconstants.HedvigBuildConstants
import com.hedvig.android.core.common.ApplicationScope
import com.hedvig.android.core.common.di.ActivityRetainedScope
import com.hedvig.android.core.common.test.MainCoroutineRule
import com.hedvig.android.core.datastore.TestPreferencesDataStore
import com.hedvig.android.core.demomode.DemoManager
import com.hedvig.android.feature.login.genericauth.GenericAuthViewModel
import com.hedvig.android.feature.login.marketing.MarketingViewModel
import com.hedvig.android.feature.login.otpinput.OtpInputViewModelFactory
import com.hedvig.android.language.LanguageService
import com.hedvig.android.language.test.FakeLanguageService
import com.hedvig.android.network.clients.TlsDiagnostics
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.createGraphFactory
import dev.zacsweers.metrox.viewmodel.MetroViewModelMultibindings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

/**
 * The feature's ViewModels and their generated factories resolve given only these six types from outside the module,
 * so a ViewModel gaining a dependency outside them fails to compile. The six are supplied here as fakes, so whether
 * they have JVM bindings is not checked by this graph.
 */
@DependencyGraph(ActivityRetainedScope::class)
internal interface LoginTestGraph : MetroViewModelMultibindings {
  @DependencyGraph.Factory
  interface Factory {
    fun create(
      @Provides authTokenService: AuthTokenService,
      @Provides authRepository: AuthRepository,
      @Provides demoManager: DemoManager,
      @Provides tlsDiagnostics: TlsDiagnostics,
      @Provides languageService: LanguageService,
      @Provides bankIdSandboxState: BankIdSandboxState,
    ): LoginTestGraph
  }
}

class JvmLoginGraphTest {
  @get:Rule
  val mainCoroutineRule = MainCoroutineRule()

  @get:Rule
  val testFolder = TemporaryFolder()

  private val testScope = TestScope()

  @Test
  fun `the feature's ViewModels resolve in a JVM graph`() {
    val graph = graph(testScope)

    assertThat(graph.viewModelProviders.keys).containsOnly(GenericAuthViewModel::class, MarketingViewModel::class)
    assertThat(graph.assistedFactoryProviders.keys).containsOnly(SwedishLoginViewModel::class)
    assertThat(graph.manualAssistedFactoryProviders.keys).containsOnly(OtpInputViewModelFactory::class)
  }

  @Test
  fun `the Swedish login ViewModel is created by its generated factory`() {
    val graph = graph(testScope)

    val factory = graph.assistedFactoryProviders.getValue(SwedishLoginViewModel::class).invoke()
    assertThat(factory).isInstanceOf<SwedishLoginViewModelFactory>()
    val viewModel = (factory as SwedishLoginViewModelFactory).create(SavedStateHandle())
    assertThat(viewModel.uiState.value.navigateToLoginScreen).isFalse()
  }

  private fun graph(scope: CoroutineScope): LoginTestGraph {
    return createGraphFactory<LoginTestGraph.Factory>().create(
      authTokenService = TestAuthTokenService(),
      authRepository = FakeAuthRepository(),
      demoManager = object : DemoManager {
        override fun isDemoMode(): Flow<Boolean> = flowOf(false)

        override suspend fun setDemoMode(demoMode: Boolean) {}
      },
      tlsDiagnostics = object : TlsDiagnostics {
        override suspend fun describe(throwable: Throwable?): String? = null
      },
      languageService = FakeLanguageService(),
      bankIdSandboxState = BankIdSandboxState(
        TestPreferencesDataStore(testFolder.newFolder(), scope),
        StagingBuildConstants,
        ApplicationScope(scope),
      ),
    )
  }
}

private object StagingBuildConstants : HedvigBuildConstants {
  override val urlGraphqlOctopus: String = ""
  override val urlBaseWeb: String = ""
  override val urlOdyssey: String = ""
  override val urlHedvigGateway: String = ""
  override val urlAuthService: String = ""
  override val urlBotService: String = ""
  override val urlClaimsService: String = ""
  override val deepLinkHosts: List<String> = emptyList()
  override val appVersionName: String = ""
  override val appVersionCode: String = ""
  override val appPackageId: String = ""
  override val isDebug: Boolean = false
  override val isProduction: Boolean = false
  override val buildApiVersion: Int = Int.MAX_VALUE
  override val platformName: String = ""
  override val model: String = ""
  override val userAgent: String = ""
}
