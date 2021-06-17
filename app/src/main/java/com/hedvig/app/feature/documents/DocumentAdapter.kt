package com.hedvig.app.feature.documents

import android.net.Uri
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hedvig.app.R
import com.hedvig.app.databinding.DocumentBinding
import com.hedvig.app.databinding.ListSubtitleItemBinding
import com.hedvig.app.feature.settings.Market
import com.hedvig.app.feature.settings.MarketManager
import com.hedvig.app.util.GenericDiffUtilItemCallback
import com.hedvig.app.util.extensions.inflate
import com.hedvig.app.util.extensions.openUri
import com.hedvig.app.util.extensions.showAlert
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.viewBinding

class DocumentAdapter(
    private val trackClick: (String) -> Unit,
    private val marketManager: MarketManager,
) : ListAdapter<DocumentItems, DocumentAdapter.DocumentsViewHolder>(GenericDiffUtilItemCallback()) {

    override fun getItemViewType(position: Int) = when (currentList[position]) {
        is DocumentItems.Document -> R.layout.document
        is DocumentItems.Header -> R.layout.list_subtitle_item
        else -> throw IllegalArgumentException("Could not find item at position $position")
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when (viewType) {
        R.layout.document -> DocumentViewHolder(parent.inflate(viewType))
        R.layout.list_subtitle_item -> TitleViewHolder(parent.inflate(viewType))
        else -> throw IllegalArgumentException("Could not find viewType $viewType")
    }

    override fun onBindViewHolder(holder: DocumentsViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is DocumentItems.Document -> (holder as DocumentViewHolder).bind(item)
            is DocumentItems.Header -> (holder as TitleViewHolder).bind(item)
        }
    }

    abstract class DocumentsViewHolder(view: View) : RecyclerView.ViewHolder(view)

    inner class TitleViewHolder(view: View) : DocumentsViewHolder(view) {
        private val binding by viewBinding(ListSubtitleItemBinding::bind)

        fun bind(header: DocumentItems.Header) {
            binding.text.text = itemView.context.getString(header.stringRes)
        }
    }

    inner class DocumentViewHolder(view: View) : DocumentsViewHolder(view) {
        private val binding by viewBinding(DocumentBinding::bind)

        fun bind(document: DocumentItems.Document) {
            val button = binding.button
            val title = document.getTitle(itemView.context)
            val subTitle = document.getSubTitle(itemView.context)

            binding.text.text = title
            binding.subtitle.text = subTitle
            binding.subtitle.isVisible = subTitle != null

            button.setHapticClickListener {
                if (title != null) {
                    trackClick(title)
                }
                // TODO Quick fix for getting new terms and conditions
                val uri = Uri.parse(
                    if (marketManager.market == Market.SE &&
                        document.type == DocumentItems.Document.Type.TERMS_AND_CONDITIONS
                    ) {
                        "https://www.hedvig.com/se/villkor"
                    } else {
                        document.url
                    }
                )
                try {
                    it.context.openUri(uri)
                } catch (throwable: Throwable) {
                    itemView.context.showAlert(
                        title = R.string.error_dialog_title,
                        message = R.string.component_error,
                        positiveAction = {}
                    )
                }
            }
        }
    }
}
