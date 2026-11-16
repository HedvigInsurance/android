package com.hedvig.android.core.buildconstants

/**
 * The value of the `User-Agent` header, in the format the platform's app sends.
 */
internal expect fun platformUserAgent(languageBCP47: String, appBuildConfig: AppBuildConfig): String
