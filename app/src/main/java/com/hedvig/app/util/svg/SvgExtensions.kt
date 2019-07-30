package com.hedvig.app.util.svg

import android.graphics.drawable.PictureDrawable
import androidx.fragment.app.Fragment

fun androidx.fragment.app.Fragment.buildRequestBuilder() = GlideApp.with(this)
    .`as`(PictureDrawable::class.java)
    .listener(SvgSoftwareLayerSetter())
