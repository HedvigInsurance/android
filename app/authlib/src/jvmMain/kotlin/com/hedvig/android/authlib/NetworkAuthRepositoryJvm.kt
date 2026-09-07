package com.hedvig.android.authlib

import io.ktor.client.engine.HttpClientEngine

/**
 * Builds an [AuthRepository] over a caller-supplied [engine], so the host application can attach its
 * own network observability to the auth client.
 *
 * This lives in the JVM source set on purpose. [HttpClientEngine] stays out of the shared surface
 * exported to Obj-C, so the framework keeps exactly one initializer for [NetworkAuthRepository].
 */
public fun networkAuthRepositoryWithEngine(
  environment: AuthEnvironment,
  additionalHttpHeadersProvider: () -> Map<String, String>,
  engine: HttpClientEngine,
): AuthRepository = NetworkAuthRepository(environment, additionalHttpHeadersProvider, engine)
