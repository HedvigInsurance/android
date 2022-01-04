package com.hedvig.app.ui.compose.composables

import android.content.res.Configuration
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidViewBinding
import com.hedvig.app.databinding.GenericErrorBinding
import com.hedvig.app.ui.compose.theme.HedvigTheme
import com.hedvig.app.util.extensions.view.setHapticClickListener

@Composable
fun GenericErrorScreen(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AndroidViewBinding(
        factory = GenericErrorBinding::inflate,
        modifier = modifier.fillMaxSize()
    ) {
        retry.setHapticClickListener {
            onClick()
        }
    }
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun GenericErrorScreenPreview() {
    HedvigTheme {
        Surface(
            color = MaterialTheme.colors.background,
        ) {
            GenericErrorScreen({})
        }
    }
}
