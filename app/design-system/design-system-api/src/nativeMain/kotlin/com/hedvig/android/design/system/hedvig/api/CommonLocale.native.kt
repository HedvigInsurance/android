package com.hedvig.android.design.system.hedvig.api

import platform.Foundation.NSLocale
import platform.Foundation.systemLocale

actual typealias CommonLocale = NSLocale

actual val previewCommonLocale: CommonLocale = NSLocale.systemLocale()
