package com.hedvig.android.memberreminders

import assertk.assertThat
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import com.hedvig.android.core.buildconstants.HedvigBuildConstants
import com.hedvig.android.core.datastore.TestPreferencesDataStore
import com.hedvig.android.test.clock.TestClock
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class EnableNotificationsReminderManagerTest {
  @get:Rule
  val testFolder = TemporaryFolder()

  @Test
  fun `Having never snoozed, showing the notification reminder is true`() = runTest {
    val clock = TestClock()
    val enableNotificationsInfoManager = EnableNotificationsReminderManagerImpl(
      TestPreferencesDataStore(
        datastoreTestFileDirectory = testFolder.newFolder("datastoreTempFolder"),
        coroutineScope = backgroundScope,
      ),
      clock,
      TestHedvigBuildConstants,
    )

    assertThat(enableNotificationsInfoManager.showNotificationReminder().first()).isTrue()
  }

  @Test
  fun `Right after snoozing, showing the notification reminder is false`() = runTest {
    val clock = TestClock()
    val enableNotificationsInfoManager = EnableNotificationsReminderManagerImpl(
      TestPreferencesDataStore(
        datastoreTestFileDirectory = testFolder.newFolder("datastoreTempFolder"),
        coroutineScope = backgroundScope,
      ),
      clock,
      TestHedvigBuildConstants,
    )

    assertThat(enableNotificationsInfoManager.showNotificationReminder().first()).isTrue()
    enableNotificationsInfoManager.snoozeNotificationReminder()
    assertThat(enableNotificationsInfoManager.showNotificationReminder().first()).isFalse()
  }

  @Test
  fun `Snoozing and waiting for 60 days still does not show the notification reminder`() = runTest {
    val clock = TestClock()
    val enableNotificationsInfoManager = EnableNotificationsReminderManagerImpl(
      TestPreferencesDataStore(
        datastoreTestFileDirectory = testFolder.newFolder("datastoreTempFolder"),
        coroutineScope = backgroundScope,
      ),
      clock,
      TestHedvigBuildConstants,
    )

    enableNotificationsInfoManager.snoozeNotificationReminder()
    clock.advanceTimeBy(60.days)
    assertThat(enableNotificationsInfoManager.showNotificationReminder().first()).isFalse()
  }

  @Test
  fun `Snoozing and waiting for 60 days + 1 second now asks for the notification reminder to show again`() = runTest {
    val clock = TestClock()
    val enableNotificationsInfoManager = EnableNotificationsReminderManagerImpl(
      TestPreferencesDataStore(
        datastoreTestFileDirectory = testFolder.newFolder("datastoreTempFolder"),
        coroutineScope = backgroundScope,
      ),
      clock,
      TestHedvigBuildConstants,
    )

    enableNotificationsInfoManager.snoozeNotificationReminder()
    assertThat(enableNotificationsInfoManager.showNotificationReminder().first()).isFalse()
    clock.advanceTimeBy(60.days + 1.seconds)
    assertThat(enableNotificationsInfoManager.showNotificationReminder().first()).isTrue()
  }

  @Test
  fun `Snoozing and letting 61 days pass many times always returns the right response back`() = runTest {
    val clock = TestClock()
    val enableNotificationsInfoManager = EnableNotificationsReminderManagerImpl(
      TestPreferencesDataStore(
        datastoreTestFileDirectory = testFolder.newFolder("datastoreTempFolder"),
        coroutineScope = backgroundScope,
      ),
      clock,
      TestHedvigBuildConstants,
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

private val TestHedvigBuildConstants = object : HedvigBuildConstants {
  override val urlGraphqlOctopus: String = ""
  override val urlBaseWeb: String = ""
  override val urlOdyssey: String = ""
  override val urlBotService: String = ""
  override val urlClaimsService: String = ""
  override val deepLinkHosts: List<String> = listOf("")
  override val appVersionName: String = ""
  override val appVersionCode: String = ""
  override val appId: String = ""
  override val isDebug: Boolean = false
  override val isProduction: Boolean = true
  override val buildApiVersion: Int = Int.MAX_VALUE
}
