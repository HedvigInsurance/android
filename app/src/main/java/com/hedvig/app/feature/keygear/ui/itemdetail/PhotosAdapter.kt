package com.hedvig.app.feature.keygear.ui.itemdetail

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.hedvig.android.owldroid.type.KeyGearItemCategory
import com.hedvig.app.R
import com.hedvig.app.databinding.KeyGearItemDetailPhotoBinding
import com.hedvig.app.feature.keygear.ui.createitem.illustration
import com.hedvig.app.feature.keygear.ui.tab.KeyGearFragment
import com.hedvig.app.util.extensions.compatColor
import com.hedvig.app.util.extensions.compatDrawable
import com.hedvig.app.util.extensions.viewBinding

class PhotosAdapter(
    firstPhotoUrl: String?,
    private val category: KeyGearItemCategory,
    private val photoDidLoad: () -> Unit
) :
    RecyclerView.Adapter<PhotosAdapter.ViewHolder>() {
    var photoUrls: List<String?> = listOf(firstPhotoUrl)
        set(value) {
            val callback = PhotosDiffCallback(field, value)
            val result = DiffUtil.calculateDiff(callback)
            result.dispatchUpdatesTo(this)
            field = value
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        LayoutInflater.from(parent.context).inflate(
            R.layout.key_gear_item_detail_photo,
            parent,
            false
        )
    )

    override fun getItemCount() = if (photoUrls.isNotEmpty()) {
        photoUrls.size
    } else {
        1
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val photoUrl = photoUrls.getOrNull(position)
        if (photoUrl != null) {
            holder.background.setBackgroundColor(Color.TRANSPARENT)
            holder.photo.updateLayoutParams {
                width = ViewGroup.LayoutParams.MATCH_PARENT
                height = ViewGroup.LayoutParams.MATCH_PARENT
            }
            Glide.with(holder.photo)
                .load(photoUrls[position])
                .transform(CenterCrop())
                .addListener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        photoDidLoad()
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        photoDidLoad()
                        return false
                    }
                })
                .into(holder.photo)
            if (position == 0) {
                holder.background.transitionName = KeyGearFragment.ITEM_BACKGROUND_TRANSITION_NAME
            }
        } else {
            holder.background.setBackgroundResource(R.drawable.background_rounded_corners)
            holder.background.backgroundTintList =
                ColorStateList.valueOf(holder.background.context.compatColor(R.color.dark_purple))
            holder.photo.updateLayoutParams {
                width = ViewGroup.LayoutParams.WRAP_CONTENT
                height = ViewGroup.LayoutParams.WRAP_CONTENT
            }
            holder.photo.setImageDrawable(holder.photo.context.compatDrawable(category.illustration))
            if (position == 0) {
                holder.background.transitionName = KeyGearFragment.ITEM_BACKGROUND_TRANSITION_NAME
            }
        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val binding by viewBinding(KeyGearItemDetailPhotoBinding::bind)
        val photo: ImageView = binding.photo
        val background: FrameLayout = binding.photoBackground
    }
}
