package com.hedvig.android.appinformation

import assertk.assertThat
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import com.hedvig.android.core.datastore.TestPreferencesDataStore
import com.hedvig.android.test.clock.TestClock
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.seconds

class EnableNotificationsInfoManagerTest {
  @get:Rule
  val testFolder = TemporaryFolder()

  @Test
  fun `Having never snoozed, showing the notification reminder is true`() = runTest {
    val clock = TestClock()
    val enableNotificationsInfoManager = EnableNotificationsInfoManagerImpl(
      TestPreferencesDataStore(
        datastoreTestFileDirectory = testFolder.newFolder("datastoreTempFolder"),
        coroutineScope = backgroundScope,
      ),
      clock,
    )

    assertThat(enableNotificationsInfoManager.showNotificationReminder().first()).isTrue()
  }

  @Test
  fun `Right after snoozing, showing the notification reminder is false`() = runTest {
    val clock = TestClock()
    val enableNotificationsInfoManager = EnableNotificationsInfoManagerImpl(
      TestPreferencesDataStore(
        datastoreTestFileDirectory = testFolder.newFolder("datastoreTempFolder"),
        coroutineScope = backgroundScope,
      ),
      clock,
    )

    assertThat(enableNotificationsInfoManager.showNotificationReminder().first()).isTrue()
    enableNotificationsInfoManager.snoozeNotificationReminder()
    assertThat(enableNotificationsInfoManager.showNotificationReminder().first()).isFalse()
  }

  @Test
  fun `Snoozing and waiting for 60 days still does not show the notification reminder`() = runTest {
    val clock = TestClock()
    val enableNotificationsInfoManager = EnableNotificationsInfoManagerImpl(
      TestPreferencesDataStore(
        datastoreTestFileDirectory = testFolder.newFolder("datastoreTempFolder"),
        coroutineScope = backgroundScope,
      ),
      clock,
    )

    enableNotificationsInfoManager.snoozeNotificationReminder()
    clock.advanceTimeBy(60.days)
    assertThat(enableNotificationsInfoManager.showNotificationReminder().first()).isFalse()
  }

  @Test
  fun `Snoozing and waiting for 60 days + 1 second now asks for the notification reminder to show again`() = runTest {
    val clock = TestClock()
    val enableNotificationsInfoManager = EnableNotificationsInfoManagerImpl(
      TestPreferencesDataStore(
        datastoreTestFileDirectory = testFolder.newFolder("datastoreTempFolder"),
        coroutineScope = backgroundScope,
      ),
      clock,
    )

    enableNotificationsInfoManager.snoozeNotificationReminder()
    assertThat(enableNotificationsInfoManager.showNotificationReminder().first()).isFalse()
    clock.advanceTimeBy(60.days + 1.seconds)
    assertThat(enableNotificationsInfoManager.showNotificationReminder().first()).isTrue()
  }

  @Test
  fun `Snoozing and letting 61 days pass many times always returns the right response back`() = runTest {
    val clock = TestClock()
    val enableNotificationsInfoManager = EnableNotificationsInfoManagerImpl(
      TestPreferencesDataStore(
        datastoreTestFileDirectory = testFolder.newFolder("datastoreTempFolder"),
        coroutineScope = backgroundScope,
      ),
      clock,
    )

    val getCurrentReminderStatus: suspend () -> Boolean = {
      enableNotificationsInfoManager.showNotificationReminder().first()
    }
    assertThat(getCurrentReminderStatus()).isTrue()
    enableNotificationsInfoManager.snoozeNotificationReminder()
    assertThat(getCurrentReminderStatus()).isFalse()
    clock.advanceTimeBy(61.days)
    assertThat(getCurrentReminderStatus()).isTrue()

    enableNotificationsInfoManager.snoozeNotificationReminder()
    assertThat(getCurrentReminderStatus()).isFalse()
    clock.advanceTimeBy(31.days)
    assertThat(getCurrentReminderStatus()).isFalse()
    clock.advanceTimeBy(31.days)
    assertThat(getCurrentReminderStatus()).isTrue()
    clock.advanceTimeBy(150.days)
    assertThat(getCurrentReminderStatus()).isTrue()
    enableNotificationsInfoManager.snoozeNotificationReminder()
    assertThat(getCurrentReminderStatus()).isFalse()
  }
}
