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
      implementation(libs.kotlinx.datetime)
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
      implementation(projects.databaseJvm)
      implementation(projects.datadogCore)
      implementation(projects.designSystemHedvig)
      implementation(projects.designSystemInternals)
      implementation(projects.experimentsPublic)
      implementation(projects.featureAddonPurchase)
      implementation(projects.featureChat)
      implementation(projects.featureChipId)
      implementation(projects.featureChooseTier)
      implementation(projects.featureChooseTierNavigation)
      implementation(projects.featureClaimChat)
      implementation(projects.featureClaimDetails)
      implementation(projects.featureClaimHistory)
      implementation(projects.featureConnectPaymentTrustly)
      implementation(projects.featureCrossSellSheet)
      implementation(projects.featureDeleteAccount)
      implementation(projects.featureEditCoinsured)
      implementation(projects.featureEditCoinsuredNavigation)
      implementation(projects.featureFlags)
      implementation(projects.featureForever)
      implementation(projects.featureForeverNavigation)
      implementation(projects.featureHelpCenter)
      implementation(projects.featureHelpCenterNavigation)
      implementation(projects.featureHome)
      implementation(projects.featureImageViewer)
      implementation(projects.featureInsuranceCertificate)
      implementation(projects.featureInsurances)
      implementation(projects.featureLogin)
      implementation(projects.featureLoginNavigation)
      implementation(projects.featureMovingflow)
      implementation(projects.featureMovingflowNavigation)
      implementation(projects.featureOnboarding)
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
      implementation(libs.apollo.normalizedCache)
      implementation(libs.assertK)
      implementation(libs.classgraph)
      implementation(libs.coroutines.test)
      implementation(libs.junit)
      implementation(libs.kotlin.reflect)
      implementation(projects.featureChatNavigation)
      implementation(projects.featureClaimDetailsNavigation)
      implementation(projects.featureConnectPaymentTrustlyNavigation)
      implementation(projects.featureHomeNavigation)
      implementation(projects.featureImageViewerNavigation)
      implementation(projects.featureInsurancesNavigation)
      implementation(projects.featureTravelCertificateNavigation)
      implementation(projects.loggingTest)
    }
  }
}

extensions.configure<ComposeExtension> {
  extensions.configure<DesktopExtension> {
    application {
      mainClass = "com.hedvig.android.desktop.MainKt"
    }
  }
}

/**
 * The JVM arguments of `run`: the backend from `-Phedvig.desktop.production`, and the Android app's version, read
 * from its build script the way `scripts/release.sh` reads it. The backend and Unleash judge the desktop app by
 * that version, so `run` fails when it cannot be read unambiguously instead of falling back to a stale one. Nothing
 * is read until `run` executes, so no other build depends on it.
 */
val desktopRunJvmArguments: Provider<List<String>> = providers
  .fileContents(isolated.rootProject.projectDirectory.file("app/app/build.gradle.kts"))
  .asText
  .zip(providers.gradleProperty("hedvig.desktop.production").orElse("false")) { androidAppBuildScript, production ->
    fun singleValue(name: String, pattern: String): String {
      val matches = Regex(pattern, RegexOption.MULTILINE).findAll(androidAppBuildScript).toList()
      if (matches.size != 1) {
        throw GradleException(
          "Expected exactly one $name in app/app/build.gradle.kts matching $pattern, found ${matches.size}. " +
            "The desktop app reports the Android app's version to the backend, so it cannot run without it.",
        )
      }
      return matches.single().groupValues[1]
    }
    val isProduction = production.toBooleanStrictOrNull()
      ?: throw GradleException("hedvig.desktop.production must be true or false, but was '$production'.")
    listOf(
      "-Dhedvig.desktop.production=$isProduction",
      "-Dhedvig.desktop.versionName=${singleValue("versionName", """^ *versionName = "([0-9]+\.[0-9]+\.[0-9]+)"$""")}",
      "-Dhedvig.desktop.versionCode=${singleValue("versionCode", """^ *versionCode = ([0-9]+)$""")}",
    )
  }

tasks.withType<JavaExec>().matching { it.name == "run" }.configureEach {
  val arguments = desktopRunJvmArguments
  jvmArgumentProviders.add(CommandLineArgumentProvider { arguments.get() })
}

tasks.withType<Test>().configureEach {
  systemProperty("java.awt.headless", "true")
}

val walkthroughTestClass = "com.hedvig.android.desktop.walkthrough.DesktopWalkthrough"

tasks.named<Test>("jvmTest") {
  filter {
    excludeTestsMatching(walkthroughTestClass)
    isFailOnNoMatchingTests = false
  }
}

/**
 * Renders the desktop app off-screen, visits every screen and writes a PNG and a status per screen. Never part of
 * `jvmTest` or CI. Every `-Phedvig.walkthrough.*` Gradle property is passed on as a system property; see
 * `DesktopWalkthrough` for the ones it reads.
 */
tasks.register<Test>("desktopWalkthrough") {
  description = "Renders every reachable desktop screen to a PNG and writes an index of outcomes."
  group = "verification"
  val jvmTest = tasks.named<Test>("jvmTest").get()
  testClassesDirs = jvmTest.testClassesDirs
  classpath = jvmTest.classpath
  filter { includeTestsMatching(walkthroughTestClass) }
  systemProperty("hedvig.walkthrough", "true")
  systemProperty(
    "hedvig.walkthrough.outputDir",
    providers.gradleProperty("hedvig.walkthrough.outputDir")
      .orElse(layout.buildDirectory.dir("walkthrough").map { it.asFile.absolutePath })
      .get(),
  )
  providers.gradlePropertiesPrefixedBy("hedvig.walkthrough.").get()
    .filterKeys { it != "hedvig.walkthrough.outputDir" }
    .forEach { (name, value) -> systemProperty(name, value) }
  maxHeapSize = "4g"
  testLogging {
    showStandardStreams = true
  }
  outputs.upToDateWhen { false }
}
