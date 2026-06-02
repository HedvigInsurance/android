package com.hedvig.android.navigation.common

import androidx.navigation3.runtime.NavKey
import kotlin.reflect.KType

interface Destination : NavKey

interface DestinationNavTypeAware {
  val typeList: List<KType>
}
