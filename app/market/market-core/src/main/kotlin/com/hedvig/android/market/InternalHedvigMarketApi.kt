package com.hedvig.android.market

@RequiresOptIn(
  level = RequiresOptIn.Level.ERROR,
  message = "This API is internal to Hedvig Market altering modules. Do not use it directly.",
)
@Retention(AnnotationRetention.BINARY)
annotation class InternalHedvigMarketApi
