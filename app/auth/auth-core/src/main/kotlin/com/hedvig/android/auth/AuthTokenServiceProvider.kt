package com.hedvig.android.auth

import com.hedvig.android.feature.demomode.DemoManager
import com.hedvig.android.feature.demomode.ProdOrDemoProvider

class AuthTokenServiceProvider(
  demoManager: DemoManager,
  prodImpl: AuthTokenService,
  demoImpl: AuthTokenService,
) : ProdOrDemoProvider<AuthTokenService>(
  demoManager = demoManager,
  demoImpl = demoImpl,
  prodImpl = prodImpl,
)
