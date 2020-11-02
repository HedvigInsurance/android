package com.hedvig.app.feature.keygear.ui.createitem

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hedvig.app.R
import com.hedvig.app.databinding.CreateKeyGearItemCategoryBinding
import com.hedvig.app.util.GenericDiffUtilItemCallback
import com.hedvig.app.util.extensions.colorAttr
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.viewBinding

class CategoryAdapter(
    private val setActiveCategory: (Category) -> Unit
) : ListAdapter<Category,CategoryAdapter.ViewHolder>(GenericDiffUtilItemCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.create_key_gear_item_category, parent, false
            )
        )


    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(getItem(position), setActiveCategory)

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val binding by viewBinding(CreateKeyGearItemCategoryBinding::bind)
        private val text: TextView = binding.text

        fun bind(data: Category, setActiveCategory: (Category) -> Unit) {
            text.text = text.resources.getString(data.category.label)
            if (data.selected) {
                text.setTextColor(text.context.colorAttr(R.attr.colorSecondary))
                text.isActivated = true
            } else {
                text.setTextColor(text.context.colorAttr(android.R.attr.textColorPrimary))
                text.isActivated = false
            }
            text.setHapticClickListener {
                setActiveCategory(data)
            }
        }
    }
}

class CategoryDiffCallback(
    private val old: List<Category>,
    private val new: List<Category>
) : DiffUtil.Callback() {
    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
        old[oldItemPosition].category == new[newItemPosition].category

    override fun getOldListSize() = old.size

    override fun getNewListSize() = new.size

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) =
        old[oldItemPosition] == new[newItemPosition]
}
