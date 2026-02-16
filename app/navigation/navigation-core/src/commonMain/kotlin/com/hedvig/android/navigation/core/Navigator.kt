package com.hedvig.android.navigation.core

import androidx.navigation.NavOptionsBuilder
import com.hedvig.android.navigation.common.Destination

interface Navigator {
  fun navigate(destination: Destination, builder: NavOptionsBuilder.() -> Unit = {})

  fun navigateUp()

  fun popBackStack()
}
