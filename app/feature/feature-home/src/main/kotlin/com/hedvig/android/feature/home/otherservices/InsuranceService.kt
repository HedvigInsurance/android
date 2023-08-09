package com.hedvig.android.feature.home.otherservices

data class InsuranceService(
  val title: String,
  val callback: () -> Unit,
)
