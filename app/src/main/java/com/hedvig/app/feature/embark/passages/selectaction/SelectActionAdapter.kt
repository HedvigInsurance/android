package com.hedvig.app.feature.embark.passages.selectaction

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hedvig.app.R
import com.hedvig.app.databinding.EmbarkSelectActionItemBinding
import com.hedvig.app.util.GenericDiffUtilItemCallback
import com.hedvig.app.util.extensions.inflate
import com.hedvig.app.util.extensions.viewBinding

class SelectActionAdapter(
    private val bindClicks: (SelectActionParameter.SelectAction, View, Int) -> Unit
) : ListAdapter<SelectActionParameter.SelectAction, SelectActionAdapter.ViewHolder>(GenericDiffUtilItemCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(parent)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), bindClicks)
    }

    class ViewHolder(parent: ViewGroup) :
        RecyclerView.ViewHolder(parent.inflate(R.layout.embark_select_action_item)) {
        private val binding by viewBinding(EmbarkSelectActionItemBinding::bind)
        fun bind(
            item: SelectActionParameter.SelectAction,
            onActionSelected: (SelectActionParameter.SelectAction, View, Int) -> Unit,
        ) {
            binding.apply {
                text.text = item.label
                onActionSelected(item, root, bindingAdapterPosition)
            }
        }
    }
}
