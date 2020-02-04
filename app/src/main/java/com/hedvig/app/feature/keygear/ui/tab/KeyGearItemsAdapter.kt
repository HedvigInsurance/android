package com.hedvig.app.feature.keygear.ui.tab

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.hedvig.app.R
import com.hedvig.app.util.extensions.view.setHapticClickListener
import kotlinx.android.synthetic.main.key_gear_add_item.view.*

class KeyGearItemsAdapter(
    private val createItem: (view: View) -> Unit
) : RecyclerView.Adapter<KeyGearItemsAdapter.ViewHolder>() {
    var items: List<KeyGearItem> = listOf()

    override fun getItemViewType(position: Int) = when (position) {
        0 -> NEW_ITEM
        else -> ITEM
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when (viewType) {
        NEW_ITEM -> {
            ViewHolder.NewItem(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.key_gear_add_item,
                    parent,
                    false
                )
            )
        }
        ITEM -> {
            ViewHolder.Item(
                LayoutInflater.from(parent.context).inflate(R.layout.key_gear_item, parent, false)
            )
        }
        else -> {
            throw Error("Invalid viewType: $viewType")
        }
    }

    override fun getItemCount() = items.size + 1

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when (holder) {
            is ViewHolder.NewItem -> {
                holder.root.setHapticClickListener { v ->
                    createItem(v)
                }
            }
            is ViewHolder.Item -> {

            }
        }
    }

    companion object {
        private const val NEW_ITEM = 0
        private const val ITEM = 1
    }

    sealed class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        class NewItem(view: View) : ViewHolder(view) {
            val root: ConstraintLayout = view.root
        }

        class Item(view: View) : ViewHolder(view)
    }
}
