package com.hedvig.android.feature.home.home.navigation

import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.navigation.common.NavKeyTypeAware
import com.hedvig.android.ui.emergency.FirstVetSection
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.typeOf
import kotlinx.serialization.Serializable

@Serializable
data object HomeKey : HedvigNavKey

@Serializable
internal data class FirstVetKey(val sections: List<FirstVetSection>) : HedvigNavKey {
  companion object : NavKeyTypeAware {
    override val typeList: List<KType> = listOf(typeOf<List<FirstVetSection>>())
  }
}

val homeCrossSellBottomSheetPermittingDestinations: List<KClass<out HedvigNavKey>> = listOf(
  HomeKey::class,
)
