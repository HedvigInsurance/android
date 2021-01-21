package com.hedvig.app.feature.keygear.ui.createitem

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
) : ListAdapter<Category, CategoryAdapter.ViewHolder>(GenericDiffUtilItemCallback()) {

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

        fun bind(data: Category, setActiveCategory: (Category) -> Unit) {
            binding.apply {
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
}
