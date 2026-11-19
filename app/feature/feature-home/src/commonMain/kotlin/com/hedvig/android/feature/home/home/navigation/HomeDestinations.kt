package com.hedvig.android.feature.home.home.navigation

import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.ui.emergency.FirstVetSection
import kotlinx.serialization.Serializable

@Serializable
internal data class FirstVetKey(val sections: List<FirstVetSection>) : HedvigNavKey
