package com.hedvig.android.data.claimintent

import kotlin.time.Instant

data class ResumableClaimIntent(
  val id: String,
  val displayName: String?,
  val startedAt: Instant,
)
