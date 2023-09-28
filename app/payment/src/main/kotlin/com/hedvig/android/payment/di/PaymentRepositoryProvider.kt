package com.hedvig.android.payment.di

import com.hedvig.android.core.demomode.DemoManager
import com.hedvig.android.core.demomode.ProdOrDemoProvider
import com.hedvig.android.payment.PaymentRepository

class PaymentRepositoryProvider(
  demoManager: DemoManager,
  demoImpl: PaymentRepository,
  prodImpl: PaymentRepository,
) : ProdOrDemoProvider<PaymentRepository>(
  demoManager = demoManager,
  demoImpl = demoImpl,
  prodImpl = prodImpl,
)
