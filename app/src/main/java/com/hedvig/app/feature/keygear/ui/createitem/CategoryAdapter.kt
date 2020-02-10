package com.hedvig.app.feature.keygear.ui.createitem

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.hedvig.app.R
import com.hedvig.app.util.extensions.compatColor
import com.hedvig.app.util.extensions.view.setHapticClickListener
import kotlinx.android.synthetic.main.create_key_gear_item_category.view.*

class CategoryAdapter(
    private val setActiveCategory: (Category) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {
    var categories: List<Category> = listOf()
        set(value) {
            val callback = CategoryDiffCallback(field, value)
            val result = DiffUtil.calculateDiff(callback)
            field = value
            result.dispatchUpdatesTo(this)
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.create_key_gear_item_category, parent, false
            )
        )

    override fun getItemCount() = categories.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(categories[position], setActiveCategory)

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val text: TextView = view.text

        fun bind(data: Category, setActiveCategory: (Category) -> Unit) {
            text.text = data.category.label
            if (data.selected) {
                text.setTextColor(text.context.compatColor(R.color.link_purple))
                text.isActivated = true
            } else {
                text.setTextColor(text.context.compatColor(R.color.text_regular))
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
