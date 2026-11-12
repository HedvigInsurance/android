package com.hedvig.android.feature.claim.details.navigation

import com.hedvig.android.navigation.common.HedvigNavKey
import kotlinx.serialization.Serializable

@Serializable
internal data class AddFilesKey(
  val targetUploadUrl: String,
  val initialFilesUri: List<String>,
) : HedvigNavKey
