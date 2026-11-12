import org.jetbrains.compose.ComposeExtension
import org.jetbrains.compose.ComposePlugin
import org.jetbrains.compose.desktop.DesktopExtension

plugins {
  id("hedvig.multiplatform.library")
  id("hedvig.gradle.plugin")
}

hedvig {
  compose()
}

kotlin {
  sourceSets {
    jvmMain.dependencies {
      implementation(ComposePlugin.DesktopDependencies.currentOs)
      implementation(libs.coil.coil)
      implementation(libs.coil.network.ktor)
      implementation(libs.coil.svg)
      implementation(libs.coroutines.core)
      implementation(libs.coroutines.swing)
      implementation(libs.jetbrains.compose.foundation)
      implementation(libs.jetbrains.compose.material3.windowSizeClass)
      implementation(libs.jetbrains.compose.runtime)
      implementation(libs.jetbrains.compose.ui)
      implementation(libs.jetbrains.lifecycle.runtime.compose)
      implementation(libs.jetbrains.lifecycle.viewmodel)
      implementation(libs.jetbrains.navigation3.ui)
      implementation(libs.jetbrains.navigationevent.compose)
      implementation(libs.kotlinx.serialization.core)
      implementation(libs.ktor.client.core)
      implementation(libs.metro.viewmodel)
      implementation(libs.metro.viewmodel.compose)
      implementation(projects.apolloAuthListeners)
      implementation(projects.apolloCore)
      implementation(projects.apolloNetworkCacheManager)
      implementation(projects.apolloOperationError)
      implementation(projects.appShell)
      implementation(projects.authCorePublic)
      implementation(projects.composeUi)
      implementation(projects.coreAppReview)
      implementation(projects.coreBuildConstants)
      implementation(projects.coreCommonPublic)
      implementation(projects.coreDatastorePublic)
      implementation(projects.coreDemoMode)
      implementation(projects.coreFileUpload)
      implementation(projects.coreIcons)
      implementation(projects.coreResources)
      implementation(projects.crossSells)
      implementation(projects.dataAddons)
      implementation(projects.dataChangetier)
      implementation(projects.dataChat)
      implementation(projects.dataClaimIntent)
      implementation(projects.dataContract)
      implementation(projects.dataConversations)
      implementation(projects.dataCrossSellAfterClaimClosed)
      implementation(projects.dataCrossSellAfterFlow)
      implementation(projects.dataPayingMember)
      implementation(projects.dataProductVariantPublic)
      implementation(projects.dataSettingsDatastorePublic)
      implementation(projects.dataTermination)
      implementation(projects.databaseCore)
      implementation(projects.datadogCore)
      implementation(projects.datadogDemoTracking)
      implementation(projects.designSystemHedvig)
      implementation(projects.designSystemInternals)
      implementation(projects.experimentsPublic)
      implementation(projects.featureAddonPurchase)
      implementation(projects.featureChatNavigation)
      implementation(projects.featureChipId)
      implementation(projects.featureChooseTier)
      implementation(projects.featureChooseTierNavigation)
      implementation(projects.featureClaimChat)
      implementation(projects.featureClaimDetailsNavigation)
      implementation(projects.featureClaimHistory)
      implementation(projects.featureConnectPaymentTrustlyNavigation)
      implementation(projects.featureCrossSellSheet)
      implementation(projects.featureDeleteAccount)
      implementation(projects.featureEditCoinsured)
      implementation(projects.featureEditCoinsuredNavigation)
      implementation(projects.featureFlags)
      implementation(projects.featureForever)
      implementation(projects.featureForeverNavigation)
      implementation(projects.featureHelpCenter)
      implementation(projects.featureHelpCenterNavigation)
      implementation(projects.featureHomeNavigation)
      implementation(projects.featureImageViewerNavigation)
      implementation(projects.featureInsuranceCertificate)
      implementation(projects.featureInsurancesNavigation)
      implementation(projects.featureLoginNavigation)
      implementation(projects.featureMovingflow)
      implementation(projects.featureMovingflowNavigation)
      implementation(projects.featureOnboardingNavigation)
      implementation(projects.featurePayinAccount)
      implementation(projects.featurePayinAccountNavigation)
      implementation(projects.featurePayments)
      implementation(projects.featurePaymentsNavigation)
      implementation(projects.featurePayoutAccount)
      implementation(projects.featureProfile)
      implementation(projects.featureProfileNavigation)
      implementation(projects.featureRemoveAddons)
      implementation(projects.featureTerminateInsurance)
      implementation(projects.featureTerminateInsuranceNavigation)
      implementation(projects.featureTravelCertificate)
      implementation(projects.featureTravelCertificateNavigation)
      implementation(projects.foreverUi)
      implementation(projects.initializable)
      implementation(projects.languageCore)
      implementation(projects.languageData)
      implementation(projects.loggingPublic)
      implementation(projects.memberQuickActions)
      implementation(projects.memberRemindersPublic)
      implementation(projects.navigationActivity)
      implementation(projects.navigationCommon)
      implementation(projects.navigationCompose)
      implementation(projects.navigationCore)
      implementation(projects.networkClients)
      implementation(projects.notificationBadgeDataPublic)
      implementation(projects.permissionCore)
      implementation(projects.theme)
      implementation(projects.tierComparison)
      implementation(projects.trackingCore)
      implementation(projects.uiForceUpgrade)
    }
    jvmTest.dependencies {
      implementation(libs.assertK)
      implementation(libs.coroutines.test)
      implementation(libs.junit)
      implementation(projects.loggingTest)
    }
  }
}

/**
 * The Android app's version, read from its build script the way `scripts/release.sh` reads it. The backend and
 * Unleash judge the desktop app by this version, so a value that cannot be read fails the build instead of
 * falling back to a stale one.
 */
val androidAppBuildScript = providers
  .fileContents(isolated.rootProject.projectDirectory.file("app/app/build.gradle.kts"))
  .asText

fun androidAppVersionValue(name: String, pattern: String): String {
  val buildScript = androidAppBuildScript.get()
  return Regex(pattern, RegexOption.MULTILINE).find(buildScript)?.groupValues?.get(1)
    ?: throw GradleException(
      "Could not read $name from app/app/build.gradle.kts with the pattern $pattern. " +
        "The desktop app reports the Android app's version to the backend, so it cannot be built without it.",
    )
}

val isProductionBackend = providers.gradleProperty("hedvig.desktop.production")
  .map { it.toBooleanStrict() }
  .orElse(false)

extensions.configure<ComposeExtension> {
  extensions.configure<DesktopExtension> {
    application {
      mainClass = "com.hedvig.android.desktop.MainKt"
      jvmArgs += listOf(
        "-Dhedvig.desktop.production=${isProductionBackend.get()}",
        "-Dhedvig.desktop.versionName=${androidAppVersionValue("versionName", """^ *versionName = "([0-9]+\.[0-9]+\.[0-9]+)"$""")}",
        "-Dhedvig.desktop.versionCode=${androidAppVersionValue("versionCode", """^ *versionCode = ([0-9]+)$""")}",
      )
    }
  }
}

tasks.withType<Test>().configureEach {
  systemProperty("java.awt.headless", "true")
}
