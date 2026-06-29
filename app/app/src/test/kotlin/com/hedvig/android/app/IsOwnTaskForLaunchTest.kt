package com.hedvig.android.app

import android.content.Intent
import assertk.assertThat
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import org.junit.Test

internal class IsOwnTaskForLaunchTest {
  @Test
  fun `a task-root launch is always own-task`() {
    assertThat(isOwnTaskForLaunch(isTaskRoot = true, launchFlags = 0)).isTrue()
    assertThat(isOwnTaskForLaunch(isTaskRoot = true, launchFlags = Intent.FLAG_ACTIVITY_NEW_TASK)).isTrue()
  }

  @Test
  fun `a non-root launch without NEW_TASK is a foreign-hosted deep link`() {
    // The notes-app deep link case: launched into the caller's task with no flags (0x0) -> keep the
    // Up/escape affordance.
    assertThat(isOwnTaskForLaunch(isTaskRoot = false, launchFlags = 0)).isFalse()
  }

  @Test
  fun `a non-root launcher relaunch with NEW_TASK is still own-task`() {
    // The reported bug: a second MainActivity stacked by a launcher relaunch (NEW_TASK |
    // BROUGHT_TO_FRONT | RESET_TASK_IF_NEEDED) is in our own task, just not its root.
    val launcherRelaunchFlags = Intent.FLAG_ACTIVITY_NEW_TASK or
      Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT or
      Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
    assertThat(isOwnTaskForLaunch(isTaskRoot = false, launchFlags = launcherRelaunchFlags)).isTrue()
  }

  @Test
  fun `a non-root notification tap that fronted our task with NEW_TASK is own-task`() {
    // The warm-notification case (1b): VIEW deep link with NEW_TASK | BROUGHT_TO_FRONT stacked on our
    // own fronted task -> own-task, so Up navigates in place instead of escaping.
    val warmNotificationFlags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT
    assertThat(isOwnTaskForLaunch(isTaskRoot = false, launchFlags = warmNotificationFlags)).isTrue()
  }
}
