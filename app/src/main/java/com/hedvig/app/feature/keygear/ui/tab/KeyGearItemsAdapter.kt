package com.hedvig.app.feature.keygear.ui.tab

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.hedvig.android.owldroid.graphql.KeyGearItemsQuery
import com.hedvig.app.BASE_MARGIN
import com.hedvig.app.R
import com.hedvig.app.databinding.KeyGearAddItemBinding
import com.hedvig.app.databinding.KeyGearItemBinding
import com.hedvig.app.feature.keygear.KeyGearTracker
import com.hedvig.app.feature.keygear.ui.createitem.illustration
import com.hedvig.app.feature.keygear.ui.createitem.label
import com.hedvig.app.util.GenericDiffUtilItemCallback
import com.hedvig.app.util.extensions.compatDrawable
import com.hedvig.app.util.extensions.view.remove
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.view.show
import com.hedvig.app.util.extensions.viewBinding

class KeyGearItemsAdapter(
    private val tracker: KeyGearTracker,
    private val createItem: (view: View) -> Unit,
    private val openItem: (root: View, item: KeyGearItemsQuery.KeyGearItem) -> Unit
) : ListAdapter<KeyGearItemsQuery.KeyGearItem, KeyGearItemsAdapter.ViewHolder>(
    GenericDiffUtilItemCallback()
) {

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

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when (holder) {
            is ViewHolder.NewItem -> {
                holder.root.setHapticClickListener { v ->
                    tracker.createItem()
                    createItem(v)
                }
            }
            is ViewHolder.Item -> {
                val item = getItem(position - 1)
                holder.root.setHapticClickListener {
                    openItem(
                        holder.root,
                        item
                    )
                }
                val photoUrl =
                    item.fragments.keyGearItemFragment.photos.getOrNull(0)?.file?.preSignedUrl
                if (photoUrl != null) {
                    holder.root.setBackgroundColor(Color.TRANSPARENT)
                    holder.image.updateLayoutParams {
                        width = ViewGroup.LayoutParams.MATCH_PARENT
                        height = ViewGroup.LayoutParams.MATCH_PARENT
                    }
                    Glide
                        .with(holder.image)
                        .load(photoUrl)
                        .transition(withCrossFade())
                        .transform(CenterCrop(), RoundedCorners(BASE_MARGIN))
                        .into(holder.image)
                } else {
                    holder.root.setBackgroundResource(R.drawable.background_rounded_corners)
                    holder.image.updateLayoutParams {
                        width = ViewGroup.LayoutParams.WRAP_CONTENT
                        height = ViewGroup.LayoutParams.WRAP_CONTENT
                    }
                    holder.image.setImageDrawable(holder.image.context.compatDrawable(item.fragments.keyGearItemFragment.category.illustration))
                }

                holder.name.text = item.fragments.keyGearItemFragment.name
                    ?: holder.name.resources.getString(item.fragments.keyGearItemFragment.category.label)

                if (item.fragments.keyGearItemFragment.physicalReferenceHash != null) {
                    holder.autoAdded.show()
                } else {
                    holder.autoAdded.remove()
                }
            }
        }
    }

    companion object {
        private const val NEW_ITEM = 0
        private const val ITEM = 1
    }

    sealed class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        class NewItem(view: View) : ViewHolder(view) {
            val binding by viewBinding(KeyGearAddItemBinding::bind)
            val root: ConstraintLayout = binding.root
        }

        class Item(view: View) : ViewHolder(view) {
            val binding by viewBinding(KeyGearItemBinding::bind)
            val root: FrameLayout = binding.keyGearItemRoot
            val image: ImageView = binding.itemPhoto
            val name: TextView = binding.name
            val autoAdded: TextView = binding.autoAddedTag
        }
    }
}
