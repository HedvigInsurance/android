package com.hedvig.android.core.buildconstants

internal actual fun platformUserAgent(languageBCP47: String, appBuildConfig: AppBuildConfig): String =
  "${appBuildConfig.applicationId} ${appBuildConfig.versionName} (iOS ${appBuildConfig.osReleaseVersion})"
