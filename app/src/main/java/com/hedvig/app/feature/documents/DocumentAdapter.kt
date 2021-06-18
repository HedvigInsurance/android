package com.hedvig.app.feature.documents

import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hedvig.app.R
import com.hedvig.app.databinding.DocumentBinding
import com.hedvig.app.databinding.ListSubtitleItemBinding
import com.hedvig.app.util.GenericDiffUtilItemCallback
import com.hedvig.app.util.extensions.inflate
import com.hedvig.app.util.extensions.tryOpenUri
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.viewBinding

class DocumentAdapter(
    private val trackClick: (String) -> Unit,
) : ListAdapter<DocumentItems, DocumentAdapter.DocumentsViewHolder>(GenericDiffUtilItemCallback()) {

    override fun getItemViewType(position: Int) = when (currentList[position]) {
        is DocumentItems.Document -> R.layout.document
        is DocumentItems.Header -> R.layout.list_subtitle_item
        else -> throw IllegalArgumentException("Could not find item at position $position")
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when (viewType) {
        R.layout.document -> DocumentViewHolder(parent.inflate(viewType), trackClick)
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

    private class DocumentViewHolder(
        view: View,
        val trackClick: (String) -> Unit
    ) : DocumentsViewHolder(view) {
        private val binding by viewBinding(DocumentBinding::bind)

        fun bind(document: DocumentItems.Document) {
            val title = document.getTitle(itemView.context)
            val subTitle = document.getSubTitle(itemView.context)

            binding.text.text = title
            binding.subtitle.text = subTitle
            binding.subtitle.isVisible = subTitle != null
            binding.button.setHapticClickListener {
                if (title != null) {
                    trackClick(title)
                }

                it.context.tryOpenUri(document.uri)
            }
        }
    }
}
