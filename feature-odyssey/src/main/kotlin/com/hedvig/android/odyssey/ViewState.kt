package com.hedvig.android.odyssey

import com.hedvig.android.odyssey.model.Claim
import com.hedvig.android.odyssey.model.Input
import com.hedvig.android.odyssey.model.Resolution
import com.hedvig.android.odyssey.repository.AutomationClaimDTO2
import com.hedvig.android.odyssey.repository.AutomationClaimInputDTO2
import com.hedvig.common.remote.file.File
import com.hedvig.common.remote.money.MonetaryAmount
import java.time.LocalDate
import java.util.UUID

data class ViewState(
  val title: String = "Submit Claim",
  val id: UUID = UUID.randomUUID(),
  val claim: Claim? = null,
  val errorMessage: String? = null,
  val isLoading: Boolean = false,
  val isLoadingPayment: Boolean = false,
  val isLastScreen: Boolean = false,
  val shouldExit: Boolean = false,
  val currentInput: Input? = null,
  val resolution: Resolution = Resolution.None,
)
