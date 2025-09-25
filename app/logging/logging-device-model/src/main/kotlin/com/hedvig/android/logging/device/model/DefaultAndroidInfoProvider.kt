package com.hedvig.android.logging.device.model

import android.os.Build
import java.util.Locale

internal class AndroidInfoProvider(
  rawDeviceBrand: String,
  rawDeviceModel: String,
) {

  constructor() : this(
    Build.BRAND.orEmpty(),
    Build.MODEL.orEmpty(),
  )

  val deviceName: String by lazy(LazyThreadSafetyMode.PUBLICATION) {
    if (deviceBrand.isBlank()) {
      deviceModel
    } else if (deviceModel.contains(deviceBrand)) {
      deviceModel
    } else {
      "$deviceBrand $deviceModel"
    }
  }

  val deviceBrand: String by lazy(LazyThreadSafetyMode.PUBLICATION) {
    rawDeviceBrand.replaceFirstChar {
      if (it.isLowerCase()) it.titlecase(Locale.US) else it.toString()
    }
  }

  val deviceModel: String = rawDeviceModel
}

