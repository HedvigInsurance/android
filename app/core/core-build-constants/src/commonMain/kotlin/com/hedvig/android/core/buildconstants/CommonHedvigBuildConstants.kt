package com.hedvig.android.core.buildconstants

import com.hedvig.android.core.buildconstants.Flavor.Develop
import com.hedvig.android.core.buildconstants.Flavor.Production
import com.hedvig.android.core.buildconstants.Flavor.Staging
import com.hedvig.android.language.LanguageService

internal class CommonHedvigBuildConstants(
  appBuildConfig: AppBuildConfig,
  languageService: LanguageService,
) : HedvigBuildConstants {
  private val appConfigUrlHolder = AppConfigUrlHolder(appBuildConfig)
  override val urlGraphqlOctopus: String = appConfigUrlHolder.urlGraphqlOctopus(appBuildConfig.appFlavor)
  override val urlBaseWeb: String = appConfigUrlHolder.urlBaseWeb(appBuildConfig.appFlavor)
  override val urlOdyssey: String = appConfigUrlHolder.urlOdyssey(appBuildConfig.appFlavor)
  override val urlBotService: String = appConfigUrlHolder.urlBotService(appBuildConfig.appFlavor)
  override val urlClaimsService: String = appConfigUrlHolder.urlClaimsService(appBuildConfig.appFlavor)
  override val deepLinkHosts: List<String> = appConfigUrlHolder.deepLinkHosts(appBuildConfig.appFlavor)
  override val appVersionName: String = appBuildConfig.versionName
  override val appVersionCode: String = appBuildConfig.versionCode.toString()

  override val appPackageId: String = appBuildConfig.applicationId

  override val isDebug: Boolean = appBuildConfig.debug
  override val isProduction: Boolean =
    appBuildConfig.buildType == "release" && appBuildConfig.applicationId == "com.hedvig.app"
  override val buildApiVersion: Int = appBuildConfig.osSdkVersion
  override val platformName: String = com.hedvig.android.core.buildconstants.platformName
  override val userAgent: String = makeUserAgent(languageService.getLanguage().toBcp47Format(), appBuildConfig)
  override val model: String = "${appBuildConfig.manufacturer} ${appBuildConfig.model}"
}

internal expect val platformName: String

private fun makeUserAgent(languageBCP47: String, appBuildConfig: AppBuildConfig): String = buildString {
  append(appBuildConfig.applicationId)
  append(" ")
  append(appBuildConfig.versionName)
  append(" ")
  append("(Android")
  append(" ")
  append(appBuildConfig.osReleaseVersion)
  append("; ")
  append(appBuildConfig.brand)
  append(" ")
  append(appBuildConfig.model)
  append("; ")
  append(appBuildConfig.device)
  append("; ")
  append(languageBCP47)
  append(")")
}

private interface UrlHolder {
  fun urlGraphqlOctopus(flavor: Flavor): String
  fun urlBaseWeb(flavor: Flavor): String
  fun urlOdyssey(flavor: Flavor): String
  fun urlBotService(flavor: Flavor): String
  fun urlClaimsService(flavor: Flavor): String
  fun deepLinkHosts(flavor: Flavor): List<String>
}

@Suppress("SpellCheckingInspection")
private class AppConfigUrlHolder(private val appBuildConfig: AppBuildConfig) : UrlHolder {
  override fun urlGraphqlOctopus(flavor: Flavor): String = when (appBuildConfig.appFlavor) {
    Production -> "https://apollo-router.prod.hedvigit.com"
    Staging -> "https://apollo-router.dev.hedvigit.com"
    Develop -> "https://apollo-router.dev.hedvigit.com"
  }

  override fun urlBaseWeb(flavor: Flavor): String = when (appBuildConfig.appFlavor) {
    Production -> "https://www.hedvig.com"
    Staging -> "https://www.dev.hedvigit.com"
    Develop -> "https://www.dev.hedvigit.com"
  }

  override fun urlOdyssey(flavor: Flavor): String = when (appBuildConfig.appFlavor) {
    Production -> "https://odyssey.prod.hedvigit.com"
    Staging -> "https://odyssey.dev.hedvigit.com"
    Develop -> "https://odyssey.dev.hedvigit.com"
  }

  override fun urlBotService(flavor: Flavor): String = when (appBuildConfig.appFlavor) {
    Production -> "https://gateway.hedvig.com/bot-service"
    Staging -> "https://gateway.dev.hedvigit.com/bot-service"
    Develop -> "https://gateway.dev.hedvigit.com/bot-service"
  }

  override fun urlClaimsService(flavor: Flavor): String = when (appBuildConfig.appFlavor) {
    Production -> "https://gateway.hedvig.com/claims"
    Staging -> "https://gateway.dev.hedvigit.com/claims"
    Develop -> "https://gateway.dev.hedvigit.com/claims"
  }

  override fun deepLinkHosts(flavor: Flavor): List<String> = when (appBuildConfig.appFlavor) {
    Production -> listOf(
      deepLinkDomainHostNew(flavor),
      deepLinkDomainHost(flavor) + deepLinkDomainPathPrefix(),
      deepLinkDomainHostOld(flavor),
    )

    Staging -> listOf(
      deepLinkDomainHostNew(flavor),
      deepLinkDomainHost(flavor) + deepLinkDomainPathPrefix(),
      deepLinkDomainHostOld(flavor),
    )

    Develop -> listOf(
      deepLinkDomainHostNew(flavor),
      deepLinkDomainHost(flavor) + deepLinkDomainPathPrefix(),
      deepLinkDomainHostOld(flavor),
    )
  }

  private fun deepLinkDomainHostNew(flavor: Flavor): String = when (flavor) {
    Production -> "link.hedvig.com"
    Staging -> "link.dev.hedvigit.com"
    Develop -> "link.dev.hedvigit.com"
  }

  private fun deepLinkDomainHost(flavor: Flavor): String = when (flavor) {
    Production -> "www.hedvig.com"
    Staging -> "dev.hedvigit.com"
    Develop -> "dev.hedvigit.com"
  }

  private fun deepLinkDomainPathPrefix(): String = "/deeplink"
  private fun deepLinkDomainHostOld(flavor: Flavor): String = when (flavor) {
    Production -> "hedvig.page.link"
    Staging -> "hedvigtest.page.link"
    Develop -> "hedvigdevelop.page.link"
  }
}
