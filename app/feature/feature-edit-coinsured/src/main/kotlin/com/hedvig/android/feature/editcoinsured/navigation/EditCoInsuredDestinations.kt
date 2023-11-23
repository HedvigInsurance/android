package com.hedvig.android.feature.editcoinsured.navigation

import com.kiwi.navigationcompose.typed.Destination
import kotlinx.serialization.Serializable

@Serializable
data class EditCoInsuredDestination(val contractId: String) : Destination
