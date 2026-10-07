package com.hedvig.android.accessibility.test

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import com.hedvig.android.design.system.hedvig.EmptyState
import com.hedvig.android.design.system.hedvig.HedvigErrorSection
import com.hedvig.android.design.system.hedvig.HedvigInformationSection
import com.hedvig.android.design.system.hedvig.HedvigNotificationCard
import com.hedvig.android.design.system.hedvig.NotificationDefaults.NotificationPriority
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class FeedbackAccessibilityTest(private val darkTheme: Boolean) {
  @get:Rule
  val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  @Test
  fun notificationInfo() = composeTestRule.assertAccessible(darkTheme) {
    HedvigNotificationCard("Your payment is on its way", NotificationPriority.Info)
  }

  @Test
  fun notificationAttention() = composeTestRule.assertAccessible(darkTheme) {
    HedvigNotificationCard("Your payment method needs updating", NotificationPriority.Attention)
  }

  @Test
  fun notificationError() = composeTestRule.assertAccessible(darkTheme) {
    HedvigNotificationCard("We could not process your payment", NotificationPriority.Error)
  }

  @Test
  fun notificationCampaign() = composeTestRule.assertAccessible(darkTheme) {
    HedvigNotificationCard("You have an unused discount", NotificationPriority.Campaign)
  }

  @Test
  fun emptyStateWithDescription() = composeTestRule.assertAccessible(darkTheme) {
    EmptyState(text = "No claims yet", description = "Claims you start will show up here.")
  }

  @Test
  fun emptyStateWithoutDescription() = composeTestRule.assertAccessible(darkTheme) {
    EmptyState(text = "No claims yet", description = null)
  }

  @Test
  fun errorSection() = composeTestRule.assertAccessible(darkTheme) {
    HedvigErrorSection(onButtonClick = {})
  }

  @Test
  fun informationSection() = composeTestRule.assertAccessible(darkTheme) {
    HedvigInformationSection(title = "Your coverage is being updated")
  }

  companion object {
    @JvmStatic
    @Parameterized.Parameters(name = "darkTheme={0}")
    fun themes(): List<Array<Any>> = listOf(arrayOf(false), arrayOf(true))
  }
}
