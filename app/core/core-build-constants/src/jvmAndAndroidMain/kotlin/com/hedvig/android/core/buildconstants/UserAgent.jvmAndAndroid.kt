package com.hedvig.android.core.buildconstants

internal actual fun platformUserAgent(languageBCP47: String, appBuildConfig: AppBuildConfig): String = buildString {
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
