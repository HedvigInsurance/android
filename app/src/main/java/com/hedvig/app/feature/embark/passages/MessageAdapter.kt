package com.hedvig.app.feature.embark.passages

import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hedvig.app.R
import com.hedvig.app.databinding.EmbarkMessageItemBinding
import com.hedvig.app.util.GenericDiffUtilItemCallback
import com.hedvig.app.util.extensions.inflate
import com.hedvig.app.util.extensions.setMarkdownText
import com.hedvig.app.util.extensions.viewBinding

class MessageAdapter(
    messages: List<String>,
) : ListAdapter<String, MessageAdapter.MessageViewHolder>(GenericDiffUtilItemCallback()) {

    init {
        submitList(messages)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = MessageViewHolder(parent)

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class MessageViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(parent.inflate(R.layout.embark_message_item)) {
        private val binding by viewBinding(EmbarkMessageItemBinding::bind)

        fun bind(message: String) {
            binding.root.setMarkdownText(message)
        }
    }
}
