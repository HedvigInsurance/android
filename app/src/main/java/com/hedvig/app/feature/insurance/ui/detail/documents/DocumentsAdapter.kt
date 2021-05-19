package com.hedvig.app.feature.insurance.ui.detail.documents

import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hedvig.app.R
import com.hedvig.app.databinding.ContractDetailDocumentsDocumentBinding
import com.hedvig.app.util.GenericDiffUtilItemCallback
import com.hedvig.app.util.extensions.canOpenUri
import com.hedvig.app.util.extensions.inflate
import com.hedvig.app.util.extensions.openUri
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.viewBinding

class DocumentsAdapter :
    ListAdapter<DocumentsModel, DocumentsAdapter.ViewHolder>(GenericDiffUtilItemCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(parent)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(parent: ViewGroup) :
        RecyclerView.ViewHolder(parent.inflate(R.layout.contract_detail_documents_document)) {
        private val binding by viewBinding(ContractDetailDocumentsDocumentBinding::bind)
        fun bind(data: DocumentsModel) = with(binding) {
            text.setText(data.label)
            subtitle.setText(data.subtitle)
            val uri = android.net.Uri.parse(data.url)
            root.setHapticClickListener {
                if (root.context.canOpenUri(uri)) {
                    root.context.openUri(uri)
                }
            }
        }
    }
}
