package com.hedvig.android.feature.onboarding.data

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import assertk.assertThat
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import java.io.File
import kotlinx.coroutines.test.runTest
import okio.Path.Companion.toPath
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class OnboardingSeenStoreTest {
  @get:Rule
  val temporaryFolder: TemporaryFolder = TemporaryFolder()

  // Note: the path must NOT exist yet; DataStore treats a pre-created empty file as corrupt.
  private fun store(): OnboardingSeenStore = DataStoreOnboardingSeenStore(
    PreferenceDataStoreFactory.createWithPath(
      produceFile = { File(temporaryFolder.root, "seen.preferences_pb").absolutePath.toPath() },
    ),
  )

  @Test
  fun `a member who never saw onboarding reads false`() = runTest {
    assertThat(store().hasSeenOnboarding("123")).isFalse()
  }

  @Test
  fun `marking seen is per member`() = runTest {
    val store = store()
    store.markOnboardingSeen("123")
    assertThat(store.hasSeenOnboarding("123")).isTrue()
    assertThat(store.hasSeenOnboarding("456")).isFalse()
  }
}
