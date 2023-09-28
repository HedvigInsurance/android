package com.hedvig.android.auth

import com.hedvig.android.core.demomode.ProdOrDemoProvider
import com.hedvig.android.feature.demomode.DemoManager

class AuthTokenServiceProvider(
  demoManager: DemoManager,
  prodImpl: AuthTokenService,
  demoImpl: AuthTokenService,
) : ProdOrDemoProvider<AuthTokenService>(
  demoManager = demoManager,
  demoImpl = demoImpl,
  prodImpl = prodImpl,
)
