package com.hedvig.app.feature.keygear.ui.itemdetail

import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.hedvig.android.owldroid.type.KeyGearItemCategory
import com.hedvig.app.R
import com.hedvig.app.feature.keygear.ui.tab.KeyGearFragment
import com.hedvig.app.util.extensions.compatColor
import kotlinx.android.synthetic.main.key_gear_item_detail_photo.view.*

class PhotosAdapter(
    firstPhotoUrl: String?,
    private val category: KeyGearItemCategory?
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

    override fun getItemCount() = photoUrls.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // TODO: Add fallback based on category-mapped asset
        Glide.with(holder.photo)
            .load(photoUrls[position])
            .placeholder(ColorDrawable(holder.photo.context.compatColor(R.color.background_elevation_1)))
            .transition(withCrossFade())
            .transform(CenterCrop())
            .into(holder.photo)


        if (position == 0) {
            holder.photo.transitionName = KeyGearFragment.ITEM_TRANSITION_NAME
        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val photo: ImageView = view.photo
    }
}
