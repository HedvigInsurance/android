package com.hedvig.android.language

/**
 * The stored choice wins, otherwise the phone's first language decides; anything that is not Swedish is English.
 *
 * Compose Multiplatform resolves strings from the first entry of the process locale list, which is exactly the stored
 * choice or the phone's first language, so this always names the language the UI is rendered in.
 */
fun resolveLanguage(storedLocaleTags: List<String>, phoneLocaleTags: List<String>): Language {
  val decidingTag = storedLocaleTags.firstOrNull() ?: phoneLocaleTags.firstOrNull() ?: return Language.EN_SE
  return languageOf(decidingTag)
}

/**
 * The language strings render in for [localeTag]: Swedish for any Swedish locale, English for everything else.
 */
fun languageOf(localeTag: String): Language {
  return if (localeTag.languageSubtag() == SWEDISH) Language.SV_SE else Language.EN_SE
}

/**
 * What to store for an in-app pick. Picking the language the phone resolves to stores nothing, so the app keeps
 * following the phone.
 */
fun storedLocaleTagsForPick(picked: Language, phoneLocaleTags: List<String>): List<String> {
  val phoneLanguage = resolveLanguage(storedLocaleTags = emptyList(), phoneLocaleTags = phoneLocaleTags)
  return if (picked == phoneLanguage) emptyList() else listOf(picked.toBcp47Format())
}

/**
 * What to store for the app to follow the phone on a platform that, with nothing stored, renders its own pick from the
 * phone's languages, [platformPickedLocaleTag]. That pick can be another language than [resolveLanguage], Swedish for
 * [Deutsch, Svenska], and then the phone's language is stored explicitly.
 */
fun localeTagsToFollowPhone(phoneLocaleTags: List<String>, platformPickedLocaleTag: String): List<String> {
  val phoneLanguage = resolveLanguage(storedLocaleTags = emptyList(), phoneLocaleTags = phoneLocaleTags)
  if (languageOf(platformPickedLocaleTag) == phoneLanguage) return emptyList()
  return listOf(phoneLanguage.toBcp47Format())
}

enum class StoredLanguageMigration {
  Keep,
  Clear,
}

/**
 * Earlier app versions stored the phone's language on every launch, so a stored value matching the phone is
 * indistinguishable from never having picked one and is cleared, as is a language the app no longer supports.
 */
fun storedLanguageMigration(storedLocaleTags: List<String>, phoneLocaleTags: List<String>): StoredLanguageMigration {
  val storedTag = storedLocaleTags.firstOrNull() ?: return StoredLanguageMigration.Keep
  if (storedTag.languageSubtag() !in setOf(SWEDISH, ENGLISH)) return StoredLanguageMigration.Clear
  val storedLanguage = resolveLanguage(storedLocaleTags = storedLocaleTags, phoneLocaleTags = emptyList())
  val phoneLanguage = resolveLanguage(storedLocaleTags = emptyList(), phoneLocaleTags = phoneLocaleTags)
  return if (storedLanguage == phoneLanguage) StoredLanguageMigration.Clear else StoredLanguageMigration.Keep
}

private const val SWEDISH = "sv"
private const val ENGLISH = "en"

private fun String.languageSubtag(): String = substringBefore('-').substringBefore('_').lowercase()
