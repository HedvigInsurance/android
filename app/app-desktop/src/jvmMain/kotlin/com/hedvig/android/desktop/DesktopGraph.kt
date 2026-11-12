package com.hedvig.android.desktop

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import coil3.ImageLoader
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.app.crosssell.GetMemberAuthorizationCodeUseCase
import com.hedvig.android.app.navigation.BackstackController
import com.hedvig.android.app.navigation.CurrentDestinationHolder
import com.hedvig.android.app.navigation.ScreenParameterExtractor
import com.hedvig.android.app.navigation.SessionReconciler
import com.hedvig.android.auth.AuthTokenService
import com.hedvig.android.auth.LogoutUseCase
import com.hedvig.android.auth.MemberIdService
import com.hedvig.android.core.appreview.WaitUntilAppReviewDialogShouldBeOpenedUseCase
import com.hedvig.android.core.buildconstants.AppBuildConfig
import com.hedvig.android.core.buildconstants.HedvigBuildConstants
import com.hedvig.android.core.common.di.ActivityRetainedScope
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.core.common.di.DesktopDataDirectory
import com.hedvig.android.core.demomode.DemoManager
import com.hedvig.android.core.tracking.EventTrackingClient
import com.hedvig.android.data.settings.datastore.SettingsDataStore
import com.hedvig.android.feature.onboarding.data.ResetOnboardingSeenUseCase
import com.hedvig.android.feature.onboarding.gate.OnboardingGate
import com.hedvig.android.featureflags.FeatureManager
import com.hedvig.android.initializable.Initializable
import com.hedvig.android.language.LanguageService
import com.hedvig.android.navigation.compose.Backstack
import com.hedvig.android.navigation.compose.HedvigDeepLinkMatcher
import com.hedvig.android.notification.badge.data.payment.PaymentsNotificationBadgeService
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Multibinds
import dev.zacsweers.metro.Provides
import dev.zacsweers.metrox.viewmodel.ViewModelGraph
import java.io.File
import kotlin.time.Clock
import kotlinx.serialization.modules.SerializersModule

/**
 * The desktop app's single graph. There is one window and no configuration change, so [ActivityRetainedScope], which
 * on Android is a per-Activity extension over [AppScope], shares the graph's lifetime through [additionalScopes].
 * The window's [BackstackController] is bound here as [Backstack].
 */
@DependencyGraph(AppScope::class, additionalScopes = [ActivityRetainedScope::class])
internal interface DesktopGraph : ViewModelGraph {
  val initializables: Set<Initializable>

  val sessionReconciler: SessionReconciler

  val settingsDataStore: SettingsDataStore

  val featureManager: FeatureManager

  val authTokenService: AuthTokenService

  val demoManager: DemoManager

  val memberIdService: MemberIdService

  val deepLinkMatcher: HedvigDeepLinkMatcher

  val imageLoader: ImageLoader

  val languageService: LanguageService

  val hedvigBuildConstants: HedvigBuildConstants

  val waitUntilAppReviewDialogShouldBeOpenedUseCase: WaitUntilAppReviewDialogShouldBeOpenedUseCase

  val logoutUseCase: LogoutUseCase

  val getMemberAuthorizationCodeUseCase: GetMemberAuthorizationCodeUseCase

  val paymentsNotificationBadgeService: PaymentsNotificationBadgeService

  val currentDestinationHolder: CurrentDestinationHolder

  val eventTrackingClient: EventTrackingClient

  val screenParameterExtractor: ScreenParameterExtractor

  val onboardingGate: OnboardingGate

  val resetOnboardingSeenUseCase: ResetOnboardingSeenUseCase

  val apolloClient: ApolloClient

  /**
   * Nav key serializers are only generated for the Android target, and the desktop back stack is never persisted,
   * so this is empty on desktop.
   */
  @Multibinds(allowEmpty = true)
  val serializersModules: Set<SerializersModule>

  @Provides
  fun bindBackstack(controller: BackstackController): Backstack = controller

  @DependencyGraph.Factory
  interface Factory {
    fun create(
      @Provides backstackController: BackstackController,
      @Provides appBuildConfig: AppBuildConfig,
      @Provides @DesktopDataDirectory dataDirectory: File,
    ): DesktopGraph
  }
}

/** The window's back stack, held in memory only: the desktop app always starts from a fresh stack. */
internal fun newBackstackController(): BackstackController = BackstackController(
  entries = mutableStateListOf(),
  parkedRuns = mutableStateMapOf(),
  pendingDeepLinkState = mutableStateOf(null),
  stashedSessionState = mutableStateOf(null),
  pendingDeepLinkStashedAtState = mutableStateOf(null),
  clock = Clock.System,
)
