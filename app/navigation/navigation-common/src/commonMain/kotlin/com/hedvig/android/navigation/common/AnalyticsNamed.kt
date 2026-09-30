package com.hedvig.android.navigation.common

/**
 * Marker letting a destination pin the name it reports to analytics, instead of having one derived
 * from its class. The value is a constant, so moving the class, renaming it, or renaming its package
 * leaves every Datadog filter and Firebase report untouched.
 *
 * Only destinations an analytics query actually names implement this. Everything else derives its
 * name from the class, which is fine right up until someone renames it.
 *
 * Declare the value as a `const val ANALYTICS_NAME`, in a companion object for a class or directly in
 * the body for an object, so `AnalyticsNameTest` can read it without constructing the key.
 */
interface AnalyticsNamed {
  val analyticsName: String
}
