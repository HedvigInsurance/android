package com.hedvig.android.feature.imageviewer.navigation

import com.hedvig.android.navigation.common.HedvigNavKey
import kotlinx.serialization.Serializable

@Serializable
data class ImageViewerKey(val imageUrl: String, val cacheKey: String) : HedvigNavKey
