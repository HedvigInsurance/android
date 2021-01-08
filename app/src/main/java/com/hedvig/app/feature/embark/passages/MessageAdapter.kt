package com.hedvig.app.feature.embark.passages

import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hedvig.app.R
import com.hedvig.app.util.GenericDiffUtilItemCallback
import com.hedvig.app.util.extensions.inflate
import kotlinx.android.synthetic.main.embark_message_item.view.*

class MessageAdapter :
    ListAdapter<String, MessageAdapter.MessageViewHolder>(GenericDiffUtilItemCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = MessageViewHolder(parent)

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class MessageViewHolder(parent: ViewGroup) :
        RecyclerView.ViewHolder(parent.inflate(R.layout.embark_message_item)) {
        private val messageView = itemView.message

        fun bind(message: String) {
            messageView.text = message
        }
    }
}
