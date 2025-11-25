package com.hedvig.android.navigation.common

import kotlin.reflect.KType

interface Destination

interface DestinationNavTypeAware {
  val typeList: List<KType>
}
