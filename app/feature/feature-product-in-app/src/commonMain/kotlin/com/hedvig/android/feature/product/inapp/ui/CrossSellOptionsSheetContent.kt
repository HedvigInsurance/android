package com.hedvig.android.feature.product.inapp.ui

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.ImageLoader
import coil3.compose.AsyncImage
import com.hedvig.android.compose.ui.EmptyContentDescription
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigTextButton
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.RadioGroup
import com.hedvig.android.design.system.hedvig.RadioOption
import com.hedvig.android.design.system.hedvig.RadioOptionId
import com.hedvig.android.design.system.hedvig.placeholder.crossSellPainterFallback
import com.hedvig.android.feature.product.inapp.navigation.CrossSellProductKey
import hedvig.resources.Res
import hedvig.resources.general_close_button
import hedvig.resources.general_continue_button
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun CrossSellOptionsSheetContent(
  options: List<CrossSellProductKey>,
  imageLoader: ImageLoader,
  onOptionChosen: (CrossSellProductKey) -> Unit,
  onClose: () -> Unit,
) {
  var selectedId by rememberSaveable { mutableStateOf<String?>(null) }
  RadioGroup(
    options = options.map { RadioOption(id = RadioOptionId(it.crossSellId), text = it.title) },
    selectedOption = selectedId?.let(::RadioOptionId),
    onRadioOptionSelected = { selectedId = it.id },
    optionIcon = { id ->
      val placeholder = crossSellPainterFallback(shape = HedvigTheme.shapes.cornerSmall)
      AsyncImage(
        model = options.firstOrNull { it.crossSellId == id.id }?.pillowImageSrc,
        contentDescription = EmptyContentDescription,
        placeholder = placeholder,
        error = placeholder,
        fallback = placeholder,
        imageLoader = imageLoader,
        contentScale = ContentScale.Crop,
        modifier = Modifier.size(32.dp),
      )
    },
  )
  Spacer(Modifier.height(16.dp))
  HedvigButton(
    text = stringResource(Res.string.general_continue_button),
    enabled = selectedId != null,
    onClick = {
      options.firstOrNull { it.crossSellId == selectedId }?.let(onOptionChosen)
    },
    modifier = Modifier.fillMaxWidth(),
  )
  Spacer(Modifier.height(8.dp))
  HedvigTextButton(
    text = stringResource(Res.string.general_close_button),
    enabled = true,
    onClick = onClose,
    modifier = Modifier.fillMaxWidth(),
  )
  Spacer(Modifier.height(8.dp))
  Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.safeDrawing))
}
