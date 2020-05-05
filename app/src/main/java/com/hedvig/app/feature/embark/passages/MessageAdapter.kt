package com.hedvig.app.feature.embark.passages

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hedvig.app.R
import kotlinx.android.synthetic.main.embark_message_item.view.*

class MessageAdapter : RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {
    var items = listOf<String>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = MessageViewHolder(parent)

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.bind(items[position])
    }

    class MessageViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
        LayoutInflater
            .from(parent.context)
            .inflate(R.layout.embark_message_item, parent, false)
    ) {
        private val messageView = itemView.message

        fun bind(message: String) {
            messageView.text = message
        }
    }
}
