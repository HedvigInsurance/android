package com.hedvig.android.data.payment.di

import com.hedvig.android.core.demomode.DemoManager
import com.hedvig.android.core.demomode.ProdOrDemoProvider
import com.hedvig.android.data.payment.PaymentRepository

class PaymentRepositoryProvider(
  override val demoManager: DemoManager,
  override val demoImpl: PaymentRepository,
  override val prodImpl: PaymentRepository,
) : ProdOrDemoProvider<PaymentRepository>
