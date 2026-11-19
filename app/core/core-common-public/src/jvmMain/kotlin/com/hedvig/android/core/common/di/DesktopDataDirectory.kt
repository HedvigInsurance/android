package com.hedvig.android.core.common.di

import dev.zacsweers.metro.Qualifier

/** The java.io.File directory the desktop app keeps its persistent files in, provided by the host application. */
@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class DesktopDataDirectory
