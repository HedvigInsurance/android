package com.hedvig.android.core.datastore

import com.hedvig.android.data.settings.datastore.AnalyticsConsent
import com.hedvig.android.data.settings.datastore.GetAnalyticsConsentUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeGetAnalyticsConsentUseCase(
  initialConsent: AnalyticsConsent? = AnalyticsConsent.NOT_DECIDED,
) : GetAnalyticsConsentUseCase {
  val consent = MutableStateFlow(initialConsent)

  override fun invoke(): Flow<AnalyticsConsent?> = consent
}
