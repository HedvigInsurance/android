package com.hedvig.android.feature.payments.data

internal interface TriggerManualChargeUseCase {
  suspend fun invoke()
}

internal class TriggerManualChargeUseCaseImpl: TriggerManualChargeUseCase {
  override suspend fun invoke() {
    TODO("Not yet implemented")
  }
}
