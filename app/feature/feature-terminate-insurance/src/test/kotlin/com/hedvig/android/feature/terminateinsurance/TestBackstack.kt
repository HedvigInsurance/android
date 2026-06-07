package com.hedvig.android.feature.terminateinsurance

import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.navigation.compose.Backstack

internal class TestBackstack(
  override val entries: MutableList<HedvigNavKey> = mutableListOf(),
) : Backstack
