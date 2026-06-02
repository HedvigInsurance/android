package com.hedvig.android.navigation.common

import androidx.navigation3.runtime.NavKey
import kotlin.reflect.KType

interface HedvigNavKey : NavKey

interface NavKeyTypeAware {
  val typeList: List<KType>
}
