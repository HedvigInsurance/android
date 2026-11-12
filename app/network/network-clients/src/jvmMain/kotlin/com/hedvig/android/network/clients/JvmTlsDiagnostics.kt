package com.hedvig.android.network.clients

import com.hedvig.android.core.common.di.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject

/**
 * Gathers no evidence, so a refused handshake is logged like any other failed request.
 */
@Inject
@ContributesBinding(AppScope::class)
internal class JvmTlsDiagnostics : TlsDiagnostics {
  override suspend fun describe(throwable: Throwable?): String? = null
}
