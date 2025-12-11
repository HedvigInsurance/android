package com.hedvig.android.core.locale

import platform.Foundation.NSLocale
import platform.Foundation.systemLocale

actual typealias CommonLocale = NSLocale

actual val previewCommonLocale: CommonLocale = NSLocale.systemLocale()
