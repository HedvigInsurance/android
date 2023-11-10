package com.hedvig.android.feature.payments.data

import com.hedvig.android.core.demomode.DemoManager
import com.hedvig.android.core.demomode.ProdOrDemoProvider

internal class PaymentRepositoryProvider(
  override val demoManager: DemoManager,
  override val demoImpl: PaymentRepository,
  override val prodImpl: PaymentRepository,
) : ProdOrDemoProvider<PaymentRepository>
