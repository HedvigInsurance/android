package com.hedvig.android.feature.odyssey.step.informdeflect

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import coil.compose.AsyncImage
import com.hedvig.android.data.claimflow.DeflectPartner
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.rememberShapedColorPainter

@Composable
internal fun PartnerImage(partner: DeflectPartner, imageLoader: ImageLoader, modifier: Modifier = Modifier) {
  AsyncImage(
    model = partner.imageUrl,
    contentDescription = null,
    imageLoader = imageLoader,
    placeholder = rememberShapedColorPainter(HedvigTheme.colorScheme.surfacePrimary),
    modifier = modifier
      .fillMaxWidth()
      .height((partner.preferredImageHeight ?: 40).dp),
  )
}
