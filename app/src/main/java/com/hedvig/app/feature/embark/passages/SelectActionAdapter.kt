package com.hedvig.app.feature.embark.passages

import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hedvig.app.R
import com.hedvig.app.databinding.EmbarkSelectActionItemBinding
import com.hedvig.app.util.GenericDiffUtilItemCallback
import com.hedvig.app.util.extensions.inflate
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.viewBinding

class SelectActionAdapter(
    private val onActionSelected: (SelectAction) -> Unit
) : ListAdapter<SelectAction, SelectActionAdapter.ViewHolder>(GenericDiffUtilItemCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(parent)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), onActionSelected)
    }

    class ViewHolder(parent: ViewGroup) :
        RecyclerView.ViewHolder(parent.inflate(R.layout.embark_select_action_item)) {
        private val binding by viewBinding(EmbarkSelectActionItemBinding::bind)
        fun bind(
            item: SelectAction,
            onActionSelected: (SelectAction) -> Unit
        ) {
            binding.apply {
                text.text = item.label
                root.setHapticClickListener { onActionSelected(item) }
            }
        }
    }
}
