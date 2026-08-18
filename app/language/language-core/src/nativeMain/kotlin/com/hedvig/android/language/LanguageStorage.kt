package com.hedvig.android.language

/**
 * iOS-side bridge that provides locale state to the shared Kotlin layer.
 * The Swift implementation must perform mutations on the main thread.
 */
interface LanguageStorage {
  fun getCurrentLanguageTag(): String

  fun getSelectedLanguageTag(): String?

  fun setLanguageTag(tag: String)
}
