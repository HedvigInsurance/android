package com.hedvig.android.feature.product.inapp.ui

import coil3.request.ImageRequest

internal actual fun ImageRequest.Builder.playAnimationOnce(): ImageRequest.Builder = this

internal actual fun ImageRequest.Builder.playAnimation(times: Int): ImageRequest.Builder = this
