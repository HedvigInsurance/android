package com.hedvig.app.feature.chat

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.hedvig.android.owldroid.graphql.GifQuery
import com.hedvig.app.R
import com.hedvig.app.util.extensions.view.setHapticClickListener
import kotlinx.android.synthetic.main.gif_item.view.*

class GifAdapter(
    private val sendGif: (String) -> Unit
) : RecyclerView.Adapter<GifAdapter.GifViewHolder>() {

    var items = listOf<GifQuery.Gif>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        GifViewHolder(
            LayoutInflater
                .from(parent.context)
                .inflate(
                    R.layout.gif_item,
                    parent,
                    false
                )
        )

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: GifViewHolder, position: Int) {
        holder.apply {
            items[position].url?.let { url ->
                Glide
                    .with(image)
                    .load(Uri.parse(url))
                    .transform(CenterCrop(), RoundedCorners(40))
                    .into(image)
                    .clearOnDetach()

                image.setHapticClickListener {
                    sendGif(url)
                }
            }
        }
    }

    override fun onViewRecycled(holder: GifViewHolder) {
        Glide
            .with(holder.image)
            .clear(holder.image)
    }

    class GifViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val image: ImageView = view.gifImage
    }
}
