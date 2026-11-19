package com.hedvig.android.feature.product.inapp.ui

import coil3.gif.repeatCount
import coil3.request.ImageRequest

internal actual fun ImageRequest.Builder.playAnimationOnce(): ImageRequest.Builder = repeatCount(0)

internal actual fun ImageRequest.Builder.playAnimation(times: Int): ImageRequest.Builder = repeatCount(times - 1)
