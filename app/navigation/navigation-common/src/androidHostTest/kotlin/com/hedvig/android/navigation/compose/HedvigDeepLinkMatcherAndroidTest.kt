package com.hedvig.android.navigation.compose

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.runner.RunWith

/**
 * Runs [HedvigDeepLinkMatcherTest] against the Android variant of the Navigation 3 runtime, which parses deep links
 * with `android.net.Uri` and so needs Robolectric's Android framework.
 */
@RunWith(AndroidJUnit4::class)
class HedvigDeepLinkMatcherAndroidTest : HedvigDeepLinkMatcherTest()
