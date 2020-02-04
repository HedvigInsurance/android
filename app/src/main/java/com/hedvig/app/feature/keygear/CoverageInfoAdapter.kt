package com.hedvig.app.feature.keygear

import android.graphics.drawable.PictureDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.hedvig.app.R
import com.hedvig.app.util.svg.GlideRequest
import kotlinx.android.synthetic.main.coverage_info_layout.view.*

class CoverageInfoAdapter(
    private val data: List<KeyGearItemCoverageBox>,
    private val requestBuilder: GlideRequest<PictureDrawable>
) :
    RecyclerView.Adapter<CoverageInfoAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        LayoutInflater.from(parent.context).inflate(
            R.layout.coverage_info_layout,
            parent,
            false
        )
    )

    override fun getItemCount() = data.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val bulletPoint = data[position]
        holder.apply {

            requestBuilder.load(bulletPoint.icon.iconByTheme(bulletPointIcon.context))
                .into(bulletPointIcon)

            bulletPointTitle.text = bulletPoint.title
            bulletPointDescription.text = bulletPoint.description
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val bulletPointIcon: ImageView = itemView.bulletPointIcon
        val bulletPointTitle: TextView = itemView.bulletPointTitle
        val bulletPointDescription: TextView = itemView.bulletPointDescription
    }
}
