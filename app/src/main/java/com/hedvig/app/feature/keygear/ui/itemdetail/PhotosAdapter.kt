package com.hedvig.app.feature.keygear.ui.itemdetail

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.hedvig.app.R
import kotlinx.android.synthetic.main.key_gear_item_detail_photo.view.*

class PhotosAdapter(
    private val photos: List<Photo>
) : RecyclerView.Adapter<PhotosAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        LayoutInflater.from(parent.context).inflate(
            R.layout.key_gear_item_detail_photo,
            parent,
            false
        )
    )

    override fun getItemCount() = photos.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Glide.with(holder.photo)
            .load(photos[position].url)
            .transform(CenterCrop())
            .into(holder.photo)
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val photo: ImageView = view.photo
    }
}

data class Photo(
    val url: String
)
