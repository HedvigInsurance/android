package com.hedvig.android.core.common.di

import org.koin.core.qualifier.qualifier

/**
 * Provided by the app module, returns the result of BuildConfig.DEBUG.
 */
val isDebugQualifier = qualifier("isDebugQualifier")
