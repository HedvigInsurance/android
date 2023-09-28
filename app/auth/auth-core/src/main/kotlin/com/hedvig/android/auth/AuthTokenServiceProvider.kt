package com.hedvig.android.auth

import com.hedvig.android.core.demomode.DemoManager
import com.hedvig.android.core.demomode.ProdOrDemoProvider

class AuthTokenServiceProvider(
  demoManager: DemoManager,
  prodImpl: AuthTokenService,
  demoImpl: AuthTokenService,
) : ProdOrDemoProvider<AuthTokenService>(
  demoManager = demoManager,
  demoImpl = demoImpl,
  prodImpl = prodImpl,
)
