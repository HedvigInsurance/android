@file:OptIn(ExperimentalObjCName::class)

package com.hedvig.android.language

import kotlin.experimental.ExperimentalObjCName
import kotlin.native.ObjCName

/**
 * The language the iOS app shows, which the iOS app owns and changes without telling Kotlin. Kotlin calls
 * [currentLanguage] for every request, from any thread, usually a background one, so an iOS implementation must be safe
 * to call from any thread and must not be isolated to the main actor.
 */
@ObjCName(name = "HedvigSharedIosLanguageSource", swiftName = "IosLanguageSource", exact = true)
interface IosLanguageSource {
  fun currentLanguage(): Language
}
