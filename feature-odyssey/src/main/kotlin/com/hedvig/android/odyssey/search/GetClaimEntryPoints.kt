package com.hedvig.android.odyssey.search

interface GetClaimEntryPoints {
  suspend operator fun invoke(): CommonClaimsResult
}
