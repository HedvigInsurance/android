package com.hedvig.android.apollo.auth.listeners

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import com.hedvig.android.apollo.NetworkCacheManager
import com.hedvig.android.auth.test.TestAuthTokenService
import com.hedvig.android.authlib.AccessToken
import com.hedvig.android.authlib.RefreshToken
import com.hedvig.android.core.common.ApplicationScope
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.language.Language
import com.hedvig.android.language.test.FakeLanguageService
import com.hedvig.android.logger.TestLogcatLoggingRule
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class LanguageSyncTest {
  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()

  @Test
  fun `the language at startup clears nothing`() = runTest {
    val sync = TestLanguageSync(this)
    sync.authTokenService.logIn()

    sync.languageSync.initialize()
    runCurrent()

    assertThat(sync.cacheManager.clearCount).isEqualTo(0)
  }

  @Test
  fun `every change while running clears the cache once`() = runTest {
    val sync = TestLanguageSync(this)
    sync.authTokenService.logIn()
    sync.languageSync.initialize()
    runCurrent()

    sync.languageService.changeLanguageExternally(Language.SV_SE)
    runCurrent()
    sync.languageService.changeLanguageExternally(Language.EN_SE)
    runCurrent()

    assertThat(sync.cacheManager.clearCount).isEqualTo(2)
  }

  @Test
  fun `a logged-in member gets a language that differs from the last upload, which is then recorded`() = runTest {
    val sync = TestLanguageSync(this)
    sync.authTokenService.logIn()
    sync.languageSync.initialize()
    runCurrent()

    sync.languageService.changeLanguageExternally(Language.SV_SE)
    runCurrent()

    assertThat(sync.uploadUseCase.uploadedLanguages).containsExactly(Language.EN_SE, Language.SV_SE)
    assertThat(sync.uploadedLanguageStore.lastUploaded).isEqualTo(Language.SV_SE)
  }

  @Test
  fun `a language equal to the last upload is not sent again`() = runTest {
    val sync = TestLanguageSync(this, lastUploaded = Language.EN_SE)
    sync.authTokenService.logIn()

    sync.languageSync.initialize()
    runCurrent()

    assertThat(sync.uploadUseCase.uploadedLanguages).isEmpty()
  }

  @Test
  fun `nothing is uploaded while logged out, but the cache is still cleared`() = runTest {
    val sync = TestLanguageSync(this)
    sync.authTokenService.logoutAndInvalidateTokens()
    sync.languageSync.initialize()
    runCurrent()

    sync.languageService.changeLanguageExternally(Language.SV_SE)
    runCurrent()

    assertThat(sync.uploadUseCase.uploadedLanguages).isEmpty()
    assertThat(sync.cacheManager.clearCount).isEqualTo(1)
  }

  @Test
  fun `a failed upload is not recorded, so it is retried later`() = runTest {
    val sync = TestLanguageSync(this, uploadSucceeds = false)
    sync.authTokenService.logIn()

    sync.languageSync.initialize()
    runCurrent()

    assertThat(sync.uploadUseCase.uploadedLanguages).containsExactly(Language.EN_SE)
    assertThat(sync.uploadedLanguageStore.lastUploaded).isNull()
  }
}

private class TestLanguageSync(
  testScope: TestScope,
  lastUploaded: Language? = null,
  uploadSucceeds: Boolean = true,
) {
  val languageService = FakeLanguageService(initialLanguage = Language.EN_SE)
  val cacheManager = CountingNetworkCacheManager()
  val authTokenService = TestAuthTokenService()
  val uploadUseCase = RecordingUploadLanguagePreferenceToBackendUseCase(uploadSucceeds)
  val uploadedLanguageStore = InMemoryUploadedLanguageStore(lastUploaded)
  val languageSync = LanguageSync(
    languageService = languageService,
    networkCacheManager = cacheManager,
    authTokenService = authTokenService,
    uploadLanguagePreferenceToBackendUseCase = uploadUseCase,
    uploadedLanguageStore = uploadedLanguageStore,
    applicationScope = ApplicationScope(testScope.backgroundScope),
  )
}

private suspend fun TestAuthTokenService.logIn() {
  loginWithTokens(AccessToken("access", 60), RefreshToken("refresh", 60))
}

private class CountingNetworkCacheManager : NetworkCacheManager {
  var clearCount = 0

  override suspend fun clearCache() {
    clearCount++
  }
}

private class RecordingUploadLanguagePreferenceToBackendUseCase(
  private val succeeds: Boolean,
) : UploadLanguagePreferenceToBackendUseCase {
  val uploadedLanguages = mutableListOf<Language>()

  override suspend fun invoke(language: Language): Either<ErrorMessage, Unit> {
    uploadedLanguages += language
    return if (succeeds) Unit.right() else ErrorMessage().left()
  }
}

private class InMemoryUploadedLanguageStore(
  var lastUploaded: Language?,
) : UploadedLanguageStore {
  override suspend fun lastUploadedLanguage(): Language? = lastUploaded

  override suspend fun setLastUploadedLanguage(language: Language) {
    lastUploaded = language
  }
}
