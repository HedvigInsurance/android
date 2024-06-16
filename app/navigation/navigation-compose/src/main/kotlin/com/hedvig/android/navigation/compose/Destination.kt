package com.hedvig.android.navigation.compose

import kotlin.reflect.KType

interface Destination

interface DestinationNavTypeAware {
  val typeList: List<KType>
}

@PublishedApi
internal val NoOpDestinationNavTypeAware = object : DestinationNavTypeAware {
  override val typeList: List<KType> = emptyList()
}
