package com.hedvig.app.feature.documents

import android.net.Uri
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.view.isVisible
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hedvig.android.core.common.android.GenericDiffUtilItemCallback
import com.hedvig.android.core.designsystem.component.button.HedvigContainedButton
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.app.R
import com.hedvig.app.databinding.DocumentBinding
import com.hedvig.app.databinding.ListSubtitleItemBinding
import com.hedvig.app.util.extensions.inflate
import com.hedvig.app.util.extensions.tryOpenUri
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.viewBinding

class DocumentAdapter(
  private val openCancelInsuranceScreen: ((insuranceId: String, insuranceDisplayName: String) -> Unit)?,
) : ListAdapter<DocumentItems, DocumentAdapter.DocumentsViewHolder>(GenericDiffUtilItemCallback()) {

  override fun getItemViewType(position: Int) = when (currentList[position]) {
    is DocumentItems.Document -> R.layout.document
    is DocumentItems.Header -> R.layout.list_subtitle_item
    is DocumentItems.CancelInsuranceButton -> TERMINATE_INSURANCE_BUTTON
    else -> error("Could not find item at position $position")
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when (viewType) {
    R.layout.document -> DocumentViewHolder(parent.inflate(viewType))
    R.layout.list_subtitle_item -> TitleViewHolder(parent.inflate(viewType))
    TERMINATE_INSURANCE_BUTTON -> CancelInsuranceButton(
      ComposeView(parent.context),
      openCancelInsuranceScreen,
    )
    else -> error("Could not find viewType $viewType")
  }

  override fun onBindViewHolder(holder: DocumentsViewHolder, position: Int) {
    when (val item = getItem(position)) {
      is DocumentItems.Document -> (holder as DocumentViewHolder).bind(item)
      is DocumentItems.Header -> (holder as TitleViewHolder).bind(item)
      is DocumentItems.CancelInsuranceButton -> (holder as CancelInsuranceButton).bind(item)
    }
  }

  abstract class DocumentsViewHolder(view: View) : RecyclerView.ViewHolder(view)

  inner class TitleViewHolder(view: View) : DocumentsViewHolder(view) {
    private val binding by viewBinding(ListSubtitleItemBinding::bind)

    fun bind(header: DocumentItems.Header) {
      binding.text.text = itemView.context.getString(header.stringRes)
    }
  }

  private class DocumentViewHolder(
    view: View,
  ) : DocumentsViewHolder(view) {
    private val binding by viewBinding(DocumentBinding::bind)

    fun bind(document: DocumentItems.Document) {
      val title = document.getTitle(itemView.context)
      val subTitle = document.getSubTitle(itemView.context)

      binding.text.text = title
      binding.subtitle.text = subTitle
      binding.subtitle.isVisible = subTitle != null
      binding.button.setHapticClickListener {
        val uri = Uri.parse(document.uriString)
        it.context.tryOpenUri(uri)
      }
    }
  }

  class CancelInsuranceButton(
    private val composeView: ComposeView,
    private val openCancelInsuranceScreen: ((insuranceId: String, insuranceDisplayName: String) -> Unit)?,
  ) : DocumentsViewHolder(composeView) {
    init {
      composeView.setViewCompositionStrategy(ViewCompositionStrategy.Default)
    }

    fun bind(cancelInsurance: DocumentItems) {
      require(cancelInsurance is DocumentItems.CancelInsuranceButton)
      composeView.setContent {
        HedvigTheme {
          HedvigContainedButton(
            onClick = {
              openCancelInsuranceScreen?.let { it(cancelInsurance.insuranceId, cancelInsurance.insuranceDisplayName) }
            },
            modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 8.dp),
          ) {
            CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurface) {
              Text(stringResource(hedvig.resources.R.string.TERMINATION_BUTTON))
            }
          }
        }
      }
    }
  }

  companion object {
    private const val TERMINATE_INSURANCE_BUTTON = 1
  }
}
