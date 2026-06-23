package com.hedvig.android.core.tracking

/**
 * Product analytics event tracking, distinct from [RumLogger] (which feeds Datadog RUM). The single binding is backed
 * by Firebase Analytics in production and is silenced in demo mode by disabling collection.
 *
 * This type is currently bound only on Android. iOS has its own independent native analytics client. To drive shared
 * (KMP) analytics through this interface on iOS, the iOS side would need to pass a native implementation into the
 * `IosGraph` when it builds the DI graph (`initDiGraph` in `shareddi`) — i.e. iOS hands us an object satisfying this
 * interface that bridges to their native tracker, contributed as the iOS binding. Until that exists, injecting this in
 * common code would fail to resolve when compiling the iOS graph (Metro is compile-time DI, so it fails loudly, not as
 * a silent no-op).
 */
interface EventTrackingClient {
  fun setCollectionEnabled(enabled: Boolean)

  fun trackEvent(name: String, parameters: Map<String, Any?> = emptyMap())

  fun trackScreen(name: String, parameters: Map<String, Any?> = emptyMap())

  fun setUserId(userId: String?)

  fun setUserProperty(name: String, value: String?)
}
