package com.hedvig.android.auth

import com.hedvig.android.core.demomode.DemoManager
import com.hedvig.android.core.demomode.ProdOrDemoProvider

class AuthTokenServiceProvider(
  override val demoManager: DemoManager,
  override val demoImpl: AuthTokenService,
  override val prodImpl: AuthTokenService,
) : ProdOrDemoProvider<AuthTokenService>
