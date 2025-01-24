package com.hedvig.android.feature.imageviewer.navigation

import com.hedvig.android.navigation.common.Destination
import kotlinx.serialization.Serializable

@Serializable
data class ImageViewer(val imageUrl: String) : Destination
