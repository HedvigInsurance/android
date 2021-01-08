package com.hedvig.app.feature.keygear.ui.itemdetail

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
        holder.bind(photoUrls[position], category, photoDidLoad, position)
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val binding by viewBinding(KeyGearItemDetailPhotoBinding::bind)
        fun bind(
            photoUrl: String?,
            category: KeyGearItemCategory,
            photoDidLoad: () -> Unit,
            position: Int
        ) {
            binding.apply {
                if (photoUrl != null) {
                    photoBackground.setBackgroundColor(Color.TRANSPARENT)
                    photo.updateLayoutParams {
                        width = ViewGroup.LayoutParams.MATCH_PARENT
                        height = ViewGroup.LayoutParams.MATCH_PARENT
                    }
                    Glide.with(photo)
                        .load(photoUrl)
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
                        .into(photo)
                    if (position == 0) {
                        photoBackground.transitionName = KeyGearFragment.ITEM_BACKGROUND_TRANSITION_NAME
                    }
                } else {
                    photoBackground.setBackgroundResource(R.drawable.background_rounded_corners)
                    photoBackground.backgroundTintList =
                        ColorStateList.valueOf(photoBackground.context.compatColor(R.color.dark_purple))
                    photo.updateLayoutParams {
                        width = ViewGroup.LayoutParams.WRAP_CONTENT
                        height = ViewGroup.LayoutParams.WRAP_CONTENT
                    }
                    photo.setImageDrawable(photo.context.compatDrawable(category.illustration))
                    if (position == 0) {
                        photoBackground.transitionName = KeyGearFragment.ITEM_BACKGROUND_TRANSITION_NAME
                    }
                }
            }
        }
    }
}
